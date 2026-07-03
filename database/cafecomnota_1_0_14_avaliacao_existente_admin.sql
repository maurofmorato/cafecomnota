-- ============================================================
-- Café com nota
-- Versão 1.0.14 - avaliação existente + base de administração
-- ============================================================

begin;

-- ------------------------------------------------------------
-- 1) Permissão administrativa
-- ------------------------------------------------------------
alter table public.usuarios
    add column if not exists is_admin boolean not null default false;

comment on column public.usuarios.is_admin is
'Define se o usuário pode acessar ações administrativas no aplicativo.';

-- Função segura para RLS e RPC do app.
create or replace function public.usuario_atual_is_admin()
returns boolean
language sql
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.usuarios u
        where u.id = auth.uid()
          and coalesce(u.is_admin, false) = true
          and coalesce(u.ativo, true) = true
    );
$$;

grant execute on function public.usuario_atual_is_admin() to anon, authenticated;

-- ------------------------------------------------------------
-- 2) Colunas auxiliares já usadas pelo app
-- ------------------------------------------------------------
alter table public.avaliacoes
    add column if not exists updated_at timestamptz default now();

alter table public.precos_cafe
    add column if not exists moeda char(3) not null default 'BRL';

alter table public.precos_cafe_historico
    add column if not exists moeda char(3) not null default 'BRL';

-- ------------------------------------------------------------
-- 3) Status/moderação em cafés e avaliações
-- ------------------------------------------------------------
alter table public.cafes
    add column if not exists status text;

update public.cafes
set status = 'ativo'
where status is null;

alter table public.cafes
    alter column status set default 'ativo',
    alter column status set not null;

alter table public.cafes
    add column if not exists moderado_por uuid references public.usuarios(id) on delete set null,
    add column if not exists moderado_em timestamptz,
    add column if not exists motivo_moderacao text;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'cafes_status_check'
          and conrelid = 'public.cafes'::regclass
    ) then
        alter table public.cafes
        add constraint cafes_status_check
        check (status in ('ativo', 'pendente', 'oculto', 'rejeitado'));
    end if;
end $$;

alter table public.avaliacoes
    add column if not exists status text;

update public.avaliacoes
set status = 'ativo'
where status is null;

alter table public.avaliacoes
    alter column status set default 'ativo',
    alter column status set not null;

alter table public.avaliacoes
    add column if not exists moderado_por uuid references public.usuarios(id) on delete set null,
    add column if not exists moderado_em timestamptz,
    add column if not exists motivo_moderacao text;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'avaliacoes_status_check'
          and conrelid = 'public.avaliacoes'::regclass
    ) then
        alter table public.avaliacoes
        add constraint avaliacoes_status_check
        check (status in ('ativo', 'pendente', 'oculto', 'rejeitado'));
    end if;
end $$;

-- ------------------------------------------------------------
-- 3) Garante upsert de avaliação por usuário + café
-- ------------------------------------------------------------
delete from public.avaliacoes a
using (
    select
        id,
        row_number() over (
            partition by cafe_id, usuario_id
            order by updated_at desc nulls last, created_at desc, id desc
        ) as ordem
    from public.avaliacoes
    where usuario_id is not null
) d
where a.id = d.id
  and d.ordem > 1;

alter table public.avaliacoes
drop constraint if exists avaliacoes_usuario_cafe_unique;

alter table public.avaliacoes
add constraint avaliacoes_usuario_cafe_unique
unique (cafe_id, usuario_id);

-- Repete a garantia do preço atual.
delete from public.precos_cafe p
using (
    select
        id,
        row_number() over (
            partition by cafe_id, usuario_id
            order by data_preco desc, updated_at desc nulls last, created_at desc, id desc
        ) as ordem
    from public.precos_cafe
    where usuario_id is not null
) d
where p.id = d.id
  and d.ordem > 1;

alter table public.precos_cafe
drop constraint if exists precos_cafe_usuario_cafe_unique;

drop index if exists public.ux_precos_cafe_usuario_cafe;
drop index if exists public.ux_precos_cafe_usuario_cafe_data;

alter table public.precos_cafe
add constraint precos_cafe_usuario_cafe_unique
unique (cafe_id, usuario_id);

-- ------------------------------------------------------------
-- 4) RLS administrativa
-- ------------------------------------------------------------
alter table public.usuarios enable row level security;
alter table public.cafes enable row level security;
alter table public.avaliacoes enable row level security;

drop policy if exists "usuarios_select_own_or_admin" on public.usuarios;
create policy "usuarios_select_own_or_admin"
on public.usuarios
for select
to authenticated
using (
    id = auth.uid()
    or public.usuario_atual_is_admin()
);

drop policy if exists "cafes_admin_update" on public.cafes;
create policy "cafes_admin_update"
on public.cafes
for update
to authenticated
using (
    public.usuario_atual_is_admin()
)
with check (
    public.usuario_atual_is_admin()
);

drop policy if exists "avaliacoes_admin_update" on public.avaliacoes;
create policy "avaliacoes_admin_update"
on public.avaliacoes
for update
to authenticated
using (
    public.usuario_atual_is_admin()
)
with check (
    public.usuario_atual_is_admin()
);

grant select on public.usuarios to authenticated;
grant update on public.cafes to authenticated;
grant update on public.avaliacoes to authenticated;

-- ------------------------------------------------------------
-- 5) View principal ignora cafés/avaliações ocultos ou pendentes
-- ------------------------------------------------------------
drop view if exists public.cafes_resumo;

create view public.cafes_resumo as
with avaliacoes_resumo as (
    select
        a.cafe_id,
        round(avg(a.nota_geral)::numeric, 2) as nota_media,
        count(*)::integer as total_avaliacoes,
        round(
            (
                100.0 * count(*) filter (where a.compraria_novamente = true)
                / nullif(count(*), 0)
            )::numeric,
            0
        )::integer as percentual_compraria_novamente,
        round(avg(a.preco_por_kg)::numeric, 2) as preco_kg_avaliacoes
    from public.avaliacoes a
    where coalesce(a.status, 'ativo') = 'ativo'
    group by a.cafe_id
),
precos_atuais_resumo as (
    select
        p.cafe_id,
        'BRL'::char(3) as moeda,
        round(avg(p.preco_kg)::numeric, 2) as preco_kg_atual_medio,
        round(avg(p.preco_250g)::numeric, 2) as preco_250g_atual_medio,
        max(p.data_preco) as ultimo_preco_em,
        count(*)::integer as total_precos
    from public.precos_cafe p
    where coalesce(p.moeda, 'BRL') = 'BRL'
    group by p.cafe_id
),
precos_historico_resumo as (
    select
        h.cafe_id,
        min(h.preco_kg) as menor_preco_kg_historico,
        max(h.preco_kg) as maior_preco_kg_historico,
        count(*)::integer as total_precos_historico
    from public.precos_cafe_historico h
    where coalesce(h.moeda, 'BRL') = 'BRL'
    group by h.cafe_id
)
select
    c.id as cafe_id,
    c.nome,
    c.marca,
    c.tipo_cafe,
    c.torra,
    c.peso_padrao_g,
    c.categoria,
    c.certificacao,
    c.origem_dado,
    c.fonte_dado,
    c.fonte_url,

    c.produto_rotulo,
    c.produtor,
    c.origem_regiao,
    c.altitude_m,
    c.variedade,
    c.processo,
    c.pontuacao_sca_min,
    c.pontuacao_sca_texto,
    c.corpo_descricao,
    c.aroma_sabor,
    c.acidez_descricao,

    ar.nota_media,
    coalesce(ar.total_avaliacoes, 0) as total_avaliacoes,
    coalesce(ar.percentual_compraria_novamente, 0) as percentual_compraria_novamente,

    coalesce(pa.preco_kg_atual_medio, ar.preco_kg_avaliacoes) as preco_kg_medio,
    coalesce(
        pa.preco_250g_atual_medio,
        round((ar.preco_kg_avaliacoes / 4.0)::numeric, 2)
    ) as preco_250g_medio,

    coalesce(pa.moeda, 'BRL'::char(3)) as moeda_preco,
    pa.ultimo_preco_em,
    coalesce(pa.total_precos, 0) as total_precos,

    ph.menor_preco_kg_historico,
    ph.maior_preco_kg_historico,
    coalesce(ph.total_precos_historico, 0) as total_precos_historico
from public.cafes c
left join avaliacoes_resumo ar
    on ar.cafe_id = c.id
left join precos_atuais_resumo pa
    on pa.cafe_id = c.id
left join precos_historico_resumo ph
    on ph.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

grant select on public.cafes_resumo to anon, authenticated;

notify pgrst, 'reload schema';

commit;

-- ============================================================
-- Conferência e ativação do administrador
-- ============================================================

-- 1) Veja os usuários disponíveis:
select
    u.id,
    au.email,
    u.apelido,
    u.nome_exibicao,
    u.is_admin,
    u.ativo
from public.usuarios u
left join auth.users au
    on au.id = u.id
order by au.email nulls last, u.created_at desc;

-- 2) Depois rode manualmente, trocando pelo seu ID:
-- update public.usuarios
-- set is_admin = true
-- where id = 'COLE_AQUI_O_ID_DO_USUARIO_ADMIN';

-- 3) Confira a função usada pelo app:
select public.usuario_atual_is_admin() as usuario_sql_editor_eh_admin;

-- ============================================================
-- Café com nota
-- Versão 1.0.15 - salvar café novo pelo aplicativo
-- ============================================================

begin;

create extension if not exists pgcrypto;

-- ------------------------------------------------------------
-- 1) Garante colunas necessárias no catálogo de cafés
-- ------------------------------------------------------------
alter table public.cafes
    add column if not exists status text,
    add column if not exists cadastrado_por uuid references public.usuarios(id) on delete set null,
    add column if not exists cadastrado_em timestamptz not null default now(),
    add column if not exists updated_at timestamptz default now(),
    add column if not exists origem_dado text default 'usuario',
    add column if not exists fonte_dado text,
    add column if not exists produto_rotulo text,
    add column if not exists motivo_moderacao text,
    add column if not exists moderado_por uuid references public.usuarios(id) on delete set null,
    add column if not exists moderado_em timestamptz;

update public.cafes
set status = 'ativo'
where status is null;

alter table public.cafes
    alter column status set default 'ativo',
    alter column status set not null;

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

-- Garante default UUID no id, caso a tabela tenha id UUID sem default.
do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'cafes'
          and column_name = 'id'
          and data_type = 'uuid'
    ) then
        alter table public.cafes
        alter column id set default gen_random_uuid();
    end if;
end $$;

-- ------------------------------------------------------------
-- 2) Garante perfil público para usuários do Auth
-- ------------------------------------------------------------
create or replace function public.criar_usuario_publico_auth()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.usuarios (
        id,
        apelido,
        nome_exibicao,
        ativo
    )
    values (
        new.id,
        coalesce(nullif(new.raw_user_meta_data->>'name', ''), split_part(new.email, '@', 1), 'usuario'),
        coalesce(nullif(new.raw_user_meta_data->>'name', ''), split_part(new.email, '@', 1), 'Usuário'),
        true
    )
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created_criar_usuario_publico on auth.users;

create trigger on_auth_user_created_criar_usuario_publico
after insert on auth.users
for each row
execute function public.criar_usuario_publico_auth();

-- Cria perfil para usuários já existentes no Auth e ainda ausentes em public.usuarios.
insert into public.usuarios (
    id,
    apelido,
    nome_exibicao,
    ativo
)
select
    au.id,
    coalesce(nullif(au.raw_user_meta_data->>'name', ''), split_part(au.email, '@', 1), 'usuario'),
    coalesce(nullif(au.raw_user_meta_data->>'name', ''), split_part(au.email, '@', 1), 'Usuário'),
    true
from auth.users au
left join public.usuarios u
    on u.id = au.id
where u.id is null;

-- ------------------------------------------------------------
-- 3) Função de admin usada pelas políticas
-- ------------------------------------------------------------
alter table public.usuarios
    add column if not exists is_admin boolean not null default false;

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
-- 4) RLS para permitir cadastro seguro pelo app
-- ------------------------------------------------------------
alter table public.cafes enable row level security;

drop policy if exists "cafes_select_active_own_or_admin" on public.cafes;
create policy "cafes_select_active_own_or_admin"
on public.cafes
for select
to anon, authenticated
using (
    coalesce(status, 'ativo') = 'ativo'
    or (
        auth.uid() is not null
        and cadastrado_por = auth.uid()
    )
    or public.usuario_atual_is_admin()
);

drop policy if exists "cafes_insert_authenticated" on public.cafes;
create policy "cafes_insert_authenticated"
on public.cafes
for insert
to authenticated
with check (
    public.usuario_atual_is_admin()
    or (
        cadastrado_por = auth.uid()
        and status = 'pendente'
    )
);

-- Mantém política administrativa de update, caso ainda não exista.
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

grant select on public.cafes to anon, authenticated;
grant insert, update on public.cafes to authenticated;

-- ------------------------------------------------------------
-- 5) View principal continua mostrando somente cafés ativos
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
-- Conferências rápidas
-- ============================================================
select
    column_name,
    data_type,
    column_default
from information_schema.columns
where table_schema = 'public'
  and table_name = 'cafes'
  and column_name in ('id', 'status', 'cadastrado_por', 'cadastrado_em', 'origem_dado', 'fonte_dado')
order by column_name;

select
    policyname,
    cmd,
    roles
from pg_policies
where schemaname = 'public'
  and tablename = 'cafes'
order by policyname;

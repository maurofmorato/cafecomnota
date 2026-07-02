-- ============================================================
-- Café com nota
-- Versão 1.0.12 - preço atual + histórico separado
--
-- Decisão de produto:
-- - public.precos_cafe guarda o preço ATUAL por usuário/café.
-- - public.precos_cafe_historico guarda cada preço informado ao longo do tempo.
-- - A média principal do app usa public.precos_cafe.
-- - O histórico não pesa várias vezes na média.
-- ============================================================

create extension if not exists pgcrypto;

create table if not exists public.precos_cafe_historico (
    id uuid primary key default gen_random_uuid(),
    cafe_id uuid not null references public.cafes(id) on delete cascade,
    usuario_id uuid references public.usuarios(id) on delete set null,
    preco_pago numeric(10,2) not null,
    peso_g numeric(10,2) not null,
    preco_kg numeric(10,2)
        generated always as (
            case
                when peso_g > 0
                then round((preco_pago * 1000.0 / peso_g), 2)
                else null
            end
        ) stored,
    preco_250g numeric(10,2)
        generated always as (
            case
                when peso_g > 0
                then round((preco_pago * 250.0 / peso_g), 2)
                else null
            end
        ) stored,
    data_preco date not null default current_date,
    origem_preco text not null default 'usuario',
    created_at timestamptz not null default now(),
    constraint precos_cafe_historico_preco_pago_positivo check (preco_pago > 0),
    constraint precos_cafe_historico_peso_g_positivo check (peso_g > 0)
);

create index if not exists idx_precos_cafe_historico_cafe_id on public.precos_cafe_historico(cafe_id);
create index if not exists idx_precos_cafe_historico_usuario_id on public.precos_cafe_historico(usuario_id);
create index if not exists idx_precos_cafe_historico_data_preco on public.precos_cafe_historico(data_preco desc);

-- Preserva registros atuais no histórico antes de limpar duplicidades.
insert into public.precos_cafe_historico (
    cafe_id, usuario_id, preco_pago, peso_g, data_preco, origem_preco, created_at
)
select
    p.cafe_id,
    p.usuario_id,
    p.preco_pago,
    p.peso_g,
    p.data_preco,
    coalesce(p.origem_preco, 'usuario'),
    p.created_at
from public.precos_cafe p
where not exists (
    select 1
    from public.precos_cafe_historico h
    where h.cafe_id = p.cafe_id
      and h.usuario_id is not distinct from p.usuario_id
      and h.preco_pago = p.preco_pago
      and h.peso_g = p.peso_g
      and h.data_preco = p.data_preco
      and h.created_at = p.created_at
);

-- Limpa duplicidades na tabela de preço atual.
-- Mantém apenas o registro mais recente por cafe_id + usuario_id.
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

-- Troca a regra única antiga pela regra nova: um preço atual por usuário/café.
drop index if exists ux_precos_cafe_usuario_cafe_data;

create unique index if not exists ux_precos_cafe_usuario_cafe
on public.precos_cafe (cafe_id, usuario_id)
where usuario_id is not null;

alter table public.precos_cafe_historico enable row level security;

drop policy if exists "precos_cafe_historico_leitura_publica" on public.precos_cafe_historico;
create policy "precos_cafe_historico_leitura_publica"
on public.precos_cafe_historico
for select
using (true);

drop policy if exists "precos_cafe_historico_insert_authenticated" on public.precos_cafe_historico;
create policy "precos_cafe_historico_insert_authenticated"
on public.precos_cafe_historico
for insert
to authenticated
with check (auth.uid() = usuario_id);

grant select on public.precos_cafe to anon, authenticated;
grant insert, update on public.precos_cafe to authenticated;
grant select on public.precos_cafe_historico to anon, authenticated;
grant insert on public.precos_cafe_historico to authenticated;

-- Recria a view de resumo.
-- A média principal usa apenas public.precos_cafe, que agora representa um preço atual por usuário/café.
drop view if exists public.cafes_resumo;

create view public.cafes_resumo as
with avaliacoes_resumo as (
    select
        a.cafe_id,
        round(avg(a.nota_geral)::numeric, 2) as nota_media,
        count(*)::integer as total_avaliacoes,
        round((100.0 * count(*) filter (where a.compraria_novamente = true) / nullif(count(*), 0))::numeric, 0)::integer as percentual_compraria_novamente,
        round(avg(a.preco_por_kg)::numeric, 2) as preco_kg_avaliacoes
    from public.avaliacoes a
    group by a.cafe_id
),
precos_atuais_resumo as (
    select
        p.cafe_id,
        round(avg(p.preco_kg)::numeric, 2) as preco_kg_atual_medio,
        round(avg(p.preco_250g)::numeric, 2) as preco_250g_atual_medio,
        max(p.data_preco) as ultimo_preco_em,
        count(*)::integer as total_precos
    from public.precos_cafe p
    group by p.cafe_id
),
precos_historico_resumo as (
    select
        h.cafe_id,
        min(h.preco_kg) as menor_preco_kg_historico,
        max(h.preco_kg) as maior_preco_kg_historico,
        count(*)::integer as total_precos_historico
    from public.precos_cafe_historico h
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
    coalesce(pa.preco_250g_atual_medio, round((ar.preco_kg_avaliacoes / 4.0)::numeric, 2)) as preco_250g_medio,
    pa.ultimo_preco_em,
    coalesce(pa.total_precos, 0) as total_precos,
    ph.menor_preco_kg_historico,
    ph.maior_preco_kg_historico,
    coalesce(ph.total_precos_historico, 0) as total_precos_historico
from public.cafes c
left join avaliacoes_resumo ar on ar.cafe_id = c.id
left join precos_atuais_resumo pa on pa.cafe_id = c.id
left join precos_historico_resumo ph on ph.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

grant select on public.cafes_resumo to anon, authenticated;

select indexname, indexdef
from pg_indexes
where schemaname = 'public'
  and tablename = 'precos_cafe'
  and indexname in ('ux_precos_cafe_usuario_cafe', 'ux_precos_cafe_usuario_cafe_data')
order by indexname;

select cafe_id, usuario_id, count(*) as quantidade
from public.precos_cafe
where usuario_id is not null
group by cafe_id, usuario_id
having count(*) > 1;

select
    cafe_id,
    nome,
    marca,
    nota_media,
    total_avaliacoes,
    preco_kg_medio,
    preco_250g_medio,
    ultimo_preco_em,
    total_precos,
    menor_preco_kg_historico,
    maior_preco_kg_historico,
    total_precos_historico
from public.cafes_resumo
where nome ilike '%Black Tucano%'
   or marca ilike '%Black Tucano%'
order by nome;

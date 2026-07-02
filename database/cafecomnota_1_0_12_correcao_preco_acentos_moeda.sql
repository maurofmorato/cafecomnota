-- ============================================================
-- Café com nota
-- Correção 1.0.12 - moeda e constraint real para preço atual
-- ============================================================

begin;

-- 1. Campos de moeda para preparar uso internacional.
alter table public.precos_cafe
    add column if not exists moeda char(3) not null default 'BRL';

alter table public.precos_cafe_historico
    add column if not exists moeda char(3) not null default 'BRL';

-- 2. Limpa duplicidades no preço atual, mantendo o registro mais recente
--    para cada café + usuário.
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

-- 3. Remove índices/constraints antigos.
alter table public.precos_cafe
drop constraint if exists precos_cafe_usuario_cafe_unique;

drop index if exists public.ux_precos_cafe_usuario_cafe;
drop index if exists public.ux_precos_cafe_usuario_cafe_data;

-- 4. Constraint única real para o upsert do app.
alter table public.precos_cafe
add constraint precos_cafe_usuario_cafe_unique
unique (cafe_id, usuario_id);

-- 5. Recria a view incluindo moeda.
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
    where p.moeda = 'BRL'
    group by p.cafe_id
),
precos_historico_resumo as (
    select
        h.cafe_id,
        min(h.preco_kg) as menor_preco_kg_historico,
        max(h.preco_kg) as maior_preco_kg_historico,
        count(*)::integer as total_precos_historico
    from public.precos_cafe_historico h
    where h.moeda = 'BRL'
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

select
    conname,
    pg_get_constraintdef(oid) as definicao
from pg_constraint
where conrelid = 'public.precos_cafe'::regclass
  and conname = 'precos_cafe_usuario_cafe_unique';

select
    column_name,
    data_type,
    column_default
from information_schema.columns
where table_schema = 'public'
  and table_name in ('precos_cafe', 'precos_cafe_historico')
  and column_name = 'moeda'
order by table_name;

-- Cafe com nota 1.0.37
-- Registra quantas avaliacoes realmente informaram custo-beneficio.
--
-- A media continua sendo a media aritmetica oficial das notas preenchidas.
-- A quantidade abaixo sera usada pelo aplicativo apenas para ordenar o
-- ranking com o ajuste bayesiano.

begin;

create or replace view public.cafes_resumo
with (security_invoker = true)
as
with avaliacoes_resumo as (
    select
        a.cafe_id,
        round(avg(a.nota_geral), 2) as nota_media,
        count(*)::integer as total_avaliacoes,
        round(
            100.0
            * count(*) filter (where a.compraria_novamente = true)::numeric
            / nullif(count(*), 0)::numeric,
            0
        )::integer as percentual_compraria_novamente,
        round(avg(a.preco_por_kg), 2) as preco_kg_avaliacoes,
        round(avg(a.aroma), 2) as aroma_media,
        round(avg(a.sabor), 2) as sabor_media,
        round(avg(a.corpo), 2) as corpo_media,
        round(avg(a.amargor), 2) as amargor_media,
        round(avg(a.acidez), 2) as acidez_media,
        round(avg(a.docura), 2) as docura_media,
        round(avg(a.custo_beneficio), 2) as custo_beneficio_media,
        count(a.custo_beneficio)::integer
            as total_avaliacoes_custo_beneficio
    from public.avaliacoes a
    where coalesce(a.status, 'ativo') = 'ativo'
    group by a.cafe_id
),
precos_atuais_resumo as (
    select
        p.cafe_id,
        'BRL'::character(3) as moeda,
        round(avg(p.preco_kg), 2) as preco_kg_atual_medio,
        round(avg(p.preco_250g), 2) as preco_250g_atual_medio,
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
    coalesce(ar.percentual_compraria_novamente, 0)
        as percentual_compraria_novamente,
    coalesce(pa.preco_kg_atual_medio, ar.preco_kg_avaliacoes)
        as preco_kg_medio,
    coalesce(
        pa.preco_250g_atual_medio,
        round(ar.preco_kg_avaliacoes / 4.0, 2)
    ) as preco_250g_medio,
    coalesce(pa.moeda, 'BRL'::character(3)) as moeda_preco,
    pa.ultimo_preco_em,
    coalesce(pa.total_precos, 0) as total_precos,
    ph.menor_preco_kg_historico,
    ph.maior_preco_kg_historico,
    coalesce(ph.total_precos_historico, 0) as total_precos_historico,
    ar.aroma_media,
    ar.sabor_media,
    ar.corpo_media,
    ar.amargor_media,
    ar.acidez_media,
    ar.docura_media,
    ar.custo_beneficio_media,
    coalesce(ar.total_avaliacoes_custo_beneficio, 0)
        as total_avaliacoes_custo_beneficio
from public.cafes c
left join avaliacoes_resumo ar on ar.cafe_id = c.id
left join precos_atuais_resumo pa on pa.cafe_id = c.id
left join precos_historico_resumo ph on ph.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

commit;

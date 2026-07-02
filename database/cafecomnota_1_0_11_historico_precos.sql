-- ============================================================
-- Café com nota
-- Versão 1.0.11 - histórico de preços mais limpo
--
-- Regra:
-- Mesmo usuário + mesmo café + mesma data -> atualiza o preço existente
-- Outro dia -> cria novo registro histórico
-- ============================================================

-- 1) Remove duplicidades antigas, mantendo o registro mais recente
--    por cafe_id + usuario_id + data_preco.
delete from public.precos_cafe p
using (
    select
        id,
        row_number() over (
            partition by cafe_id, usuario_id, data_preco
            order by created_at desc, id desc
        ) as ordem
    from public.precos_cafe
    where usuario_id is not null
) d
where p.id = d.id
  and d.ordem > 1;

-- 2) Cria índice único para permitir upsert pelo app.
create unique index if not exists ux_precos_cafe_usuario_cafe_data
on public.precos_cafe (
    cafe_id,
    usuario_id,
    data_preco
);

-- 3) Garante grants.
grant select on public.precos_cafe to anon, authenticated;
grant insert, update on public.precos_cafe to authenticated;

-- 4) Recria a view de resumo.
--    Preço vem preferencialmente de public.precos_cafe.
--    public.avaliacoes fica apenas como fallback para cafés antigos
--    que ainda não tenham preço em precos_cafe.
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
precos_resumo as (
    select
        p.cafe_id,
        round(
            avg(p.preco_kg) filter (
                where p.data_preco >= current_date - interval '90 days'
            )::numeric,
            2
        ) as preco_kg_90d,
        round(avg(p.preco_kg)::numeric, 2) as preco_kg_geral,
        round(
            avg(p.preco_250g) filter (
                where p.data_preco >= current_date - interval '90 days'
            )::numeric,
            2
        ) as preco_250g_90d,
        round(avg(p.preco_250g)::numeric, 2) as preco_250g_geral,
        max(p.data_preco) as ultimo_preco_em,
        count(*)::integer as total_precos
    from public.precos_cafe p
    group by p.cafe_id
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

    coalesce(
        pr.preco_kg_90d,
        pr.preco_kg_geral,
        ar.preco_kg_avaliacoes
    ) as preco_kg_medio,

    coalesce(
        pr.preco_250g_90d,
        pr.preco_250g_geral,
        round((ar.preco_kg_avaliacoes / 4.0)::numeric, 2)
    ) as preco_250g_medio,

    pr.ultimo_preco_em,
    coalesce(pr.total_precos, 0) as total_precos
from public.cafes c
left join avaliacoes_resumo ar
    on ar.cafe_id = c.id
left join precos_resumo pr
    on pr.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

grant select on public.cafes_resumo to anon, authenticated;

-- 5) Conferência de possíveis duplicidades restantes.
select
    cafe_id,
    usuario_id,
    data_preco,
    count(*) as quantidade
from public.precos_cafe
where usuario_id is not null
group by
    cafe_id,
    usuario_id,
    data_preco
having count(*) > 1
order by quantidade desc;

-- 6) Conferência do Black Tucano.
select
    cafe_id,
    nome,
    marca,
    nota_media,
    total_avaliacoes,
    preco_kg_medio,
    preco_250g_medio,
    ultimo_preco_em,
    total_precos
from public.cafes_resumo
where nome ilike '%Black Tucano%'
   or marca ilike '%Black Tucano%'
order by nome;

-- ============================================================
-- Café com nota
-- Versão 1.0.8 - ficha técnica e catálogo especial
-- Execute no Supabase SQL Editor.
-- ============================================================

alter table public.cafes
    add column if not exists categoria text,
    add column if not exists certificacao text,
    add column if not exists origem_dado text default 'usuario',
    add column if not exists fonte_dado text,
    add column if not exists fonte_url text,
    add column if not exists updated_at timestamptz default now(),
    add column if not exists produto_rotulo text,
    add column if not exists produtor text,
    add column if not exists origem_regiao text,
    add column if not exists altitude_m integer,
    add column if not exists variedade text,
    add column if not exists processo text,
    add column if not exists pontuacao_sca_min numeric(5,2),
    add column if not exists pontuacao_sca_texto text,
    add column if not exists corpo_descricao text,
    add column if not exists aroma_sabor text,
    add column if not exists acidez_descricao text;

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
        round(avg(p.preco_kg)::numeric, 2) as preco_kg_geral,
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
    coalesce(pr.preco_kg_geral, ar.preco_kg_avaliacoes) as preco_kg_medio,
    coalesce(pr.preco_250g_geral, round((ar.preco_kg_avaliacoes / 4.0)::numeric, 2)) as preco_250g_medio,
    pr.ultimo_preco_em,
    coalesce(pr.total_precos, 0) as total_precos
from public.cafes c
left join avaliacoes_resumo ar
    on ar.cafe_id = c.id
left join precos_resumo pr
    on pr.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

grant select on public.cafes_resumo to anon, authenticated;

insert into public.cafes (
    nome,
    marca,
    tipo_cafe,
    torra,
    peso_padrao_g,
    categoria,
    certificacao,
    origem_dado,
    fonte_dado,
    fonte_url,
    status,
    produto_rotulo,
    produtor,
    origem_regiao,
    altitude_m,
    variedade,
    processo,
    pontuacao_sca_min,
    pontuacao_sca_texto,
    corpo_descricao,
    aroma_sabor,
    acidez_descricao
)
values (
    'Black Tucano Honey Coffee',
    'Black Tucano Coffee Roasters',
    'grao',
    'media',
    250,
    'Especial',
    'SCA',
    'catalogo_especial',
    'embalagem',
    null,
    'ativo',
    'Café Black Tucano Honey Coffee Torrado e em Grãos 250g',
    'Waldir Manske',
    'Sítio Alto Santa Joana – Afonso Cláudio, Espírito Santo',
    1100,
    'Caturra Amarelo',
    'Honey Coffee',
    86,
    'Acima de 86 pontos',
    'Aveludado',
    'Melaço de cana, mel e framboesa',
    'Média e arredondada'
)
on conflict do nothing;

update public.cafes
set
    produto_rotulo = 'Café Black Tucano Honey Coffee Torrado e em Grãos 250g',
    produtor = 'Waldir Manske',
    origem_regiao = 'Sítio Alto Santa Joana – Afonso Cláudio, Espírito Santo',
    altitude_m = 1100,
    variedade = 'Caturra Amarelo',
    processo = 'Honey Coffee',
    pontuacao_sca_min = 86,
    pontuacao_sca_texto = 'Acima de 86 pontos',
    corpo_descricao = 'Aveludado',
    aroma_sabor = 'Melaço de cana, mel e framboesa',
    acidez_descricao = 'Média e arredondada',
    peso_padrao_g = coalesce(peso_padrao_g, 250),
    categoria = coalesce(categoria, 'Especial'),
    certificacao = coalesce(certificacao, 'SCA'),
    origem_dado = 'catalogo_especial',
    fonte_dado = 'embalagem',
    updated_at = now()
where lower(nome) in (
    lower('Black Tucano Honey Coffee'),
    lower('Black Tucano Coffee')
)
or lower(marca) = lower('Black Tucano Coffee Roasters');

select
    cafe_id,
    nome,
    marca,
    categoria,
    certificacao,
    produto_rotulo,
    produtor,
    origem_regiao,
    altitude_m,
    variedade,
    processo,
    pontuacao_sca_texto,
    corpo_descricao,
    aroma_sabor,
    acidez_descricao,
    nota_media,
    total_avaliacoes,
    preco_kg_medio
from public.cafes_resumo
where nome ilike '%Black Tucano%'
   or marca ilike '%Black Tucano%'
order by nome;

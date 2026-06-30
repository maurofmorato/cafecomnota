-- ============================================================
-- Café com nota
-- Correção 1.0.7 - carga inicial com tipo_cafe técnico
-- Motivo: public.cafes.tipo_cafe aceita valores como grao, moido, capsula.
-- ============================================================

-- Confira a constraint, se quiser:
-- select conname, pg_get_constraintdef(oid)
-- from pg_constraint
-- where conrelid = 'public.cafes'::regclass
--   and conname = 'cafes_tipo_cafe_chk';

with seed(nome, marca, tipo_cafe, torra, categoria) as (
values
    ('3 Corações Tradicional', '3 Corações', 'moido', 'media', 'Tradicional'),
    ('3 Corações Extra Forte', '3 Corações', 'moido', 'escura', 'Extraforte'),
    ('3 Corações Gourmet', '3 Corações', 'moido', 'media', 'Gourmet'),
    ('3 Corações Rituais Cerrado Mineiro', '3 Corações', 'moido', 'media', 'Especial'),
    ('3 Corações Rituais Mogiana Paulista', '3 Corações', 'moido', 'media', 'Especial'),
    ('3 Corações Rituais Sul de Minas', '3 Corações', 'moido', 'media', 'Especial'),
    ('3 Corações Portinari', '3 Corações', 'moido', 'media', 'Gourmet'),
    ('Pilão Tradicional', 'Pilão', 'moido', 'media', 'Tradicional'),
    ('Pilão Extra Forte', 'Pilão', 'moido', 'escura', 'Extraforte'),
    ('Pilão Intenso', 'Pilão', 'moido', 'escura', 'Tradicional'),
    ('Pilão Cafeteria Espresso', 'Pilão', 'grao', 'media', 'Gourmet'),
    ('Pilão Cafeteria Torrado e Moído', 'Pilão', 'moido', 'media', 'Superior'),
    ('Melitta Tradicional', 'Melitta', 'moido', 'media', 'Tradicional'),
    ('Melitta Extra Forte', 'Melitta', 'moido', 'escura', 'Extraforte'),
    ('Melitta Especial', 'Melitta', 'moido', 'media', 'Superior'),
    ('Melitta Regiões Brasileiras Sul de Minas', 'Melitta', 'moido', 'media', 'Especial'),
    ('Melitta Regiões Brasileiras Cerrado', 'Melitta', 'moido', 'media', 'Especial'),
    ('Melitta Regiões Brasileiras Mogiana', 'Melitta', 'moido', 'media', 'Especial'),
    ('Café do Ponto Tradicional', 'Café do Ponto', 'moido', 'media', 'Tradicional'),
    ('Café do Ponto Extra Forte', 'Café do Ponto', 'moido', 'escura', 'Extraforte'),
    ('Café do Ponto Exportação', 'Café do Ponto', 'moido', 'media', 'Superior'),
    ('Café do Ponto Safra Especial', 'Café do Ponto', 'moido', 'media', 'Especial'),
    ('Café Brasileiro Tradicional', 'Café Brasileiro', 'moido', 'media', 'Tradicional'),
    ('Café Brasileiro Extra Forte', 'Café Brasileiro', 'moido', 'escura', 'Extraforte'),
    ('Café Brasileiro Superior', 'Café Brasileiro', 'moido', 'media', 'Superior'),
    ('Caboclo Tradicional', 'Caboclo', 'moido', 'media', 'Tradicional'),
    ('Caboclo Extra Forte', 'Caboclo', 'moido', 'escura', 'Extraforte'),
    ('Café Pelé Tradicional', 'Café Pelé', 'moido', 'media', 'Tradicional'),
    ('Café Pelé Extra Forte', 'Café Pelé', 'moido', 'escura', 'Extraforte'),
    ('Café União Tradicional', 'Café União', 'moido', 'media', 'Tradicional'),
    ('Café União Extra Forte', 'Café União', 'moido', 'escura', 'Extraforte'),
    ('Santa Clara Tradicional', 'Santa Clara', 'moido', 'media', 'Tradicional'),
    ('Santa Clara Extra Forte', 'Santa Clara', 'moido', 'escura', 'Extraforte'),
    ('Santa Clara Reserva da Família', 'Santa Clara', 'moido', 'media', 'Superior'),
    ('Pimpinela Tradicional', 'Pimpinela', 'moido', 'media', 'Tradicional'),
    ('Pimpinela Extra Forte', 'Pimpinela', 'moido', 'escura', 'Extraforte'),
    ('Pimpinela Gourmet', 'Pimpinela', 'moido', 'media', 'Gourmet'),
    ('Orfeu Intenso', 'Orfeu', 'grao', 'media', 'Especial'),
    ('Orfeu Clássico', 'Orfeu', 'grao', 'media', 'Especial'),
    ('Orfeu Bourbon Amarelo', 'Orfeu', 'grao', 'media', 'Especial'),
    ('Orfeu Orgânico', 'Orfeu', 'grao', 'media', 'Especial'),
    ('Baggio Bourbon', 'Baggio', 'moido', 'media', 'Gourmet'),
    ('Baggio Chocolate Trufado', 'Baggio', 'moido', 'media', 'Aromatizado'),
    ('Baggio Aroma de Avelã', 'Baggio', 'moido', 'media', 'Aromatizado'),
    ('Baggio Gourmet', 'Baggio', 'moido', 'media', 'Gourmet'),
    ('Octavio Café Bourbon Amarelo', 'Octavio Café', 'grao', 'media', 'Especial'),
    ('Octavio Café Blend', 'Octavio Café', 'grao', 'media', 'Especial'),
    ('Santo Grão Blend', 'Santo Grão', 'grao', 'media', 'Especial'),
    ('Coffee++ Frutado', 'Coffee++', 'grao', 'media_clara', 'Especial'),
    ('Coffee++ Chocolate', 'Coffee++', 'grao', 'media', 'Especial'),
    ('Unique Cafés Frutado', 'Unique Cafés', 'grao', 'media_clara', 'Especial'),
    ('Unique Cafés Bourbon Amarelo', 'Unique Cafés', 'grao', 'media', 'Especial'),
    ('Lucca Cafés Espresso', 'Lucca Cafés', 'grao', 'media', 'Especial'),
    ('Lucca Cafés Clássico', 'Lucca Cafés', 'grao', 'media', 'Especial'),
    ('Dutras Coffee Catuaí Vermelho', 'Dutras Coffee', 'grao', 'media', 'Especial'),
    ('Fazenda Pessegueiro Especial', 'Fazenda Pessegueiro', 'grao', 'media', 'Especial'),
    ('Fazenda Sertão Bourbon Amarelo', 'Fazenda Sertão', 'grao', 'media', 'Especial'),
    ('Um Coffee Co. Espresso', 'Um Coffee Co.', 'grao', 'media', 'Especial'),
    ('Isso é Café Blend', 'Isso é Café', 'grao', 'media', 'Especial'),
    ('Black Tucano Coffee', 'Black Tucano', 'grao', 'media', 'Especial'),
    ('Suplicy Espresso', 'Suplicy', 'grao', 'media', 'Especial'),
    ('Starbucks Pike Place Roast', 'Starbucks', 'moido', 'media', 'Gourmet'),
    ('Starbucks Espresso Roast', 'Starbucks', 'grao', 'escura', 'Gourmet')
)
insert into public.cafes (
    nome,
    marca,
    tipo_cafe,
    torra,
    categoria,
    certificacao,
    origem_dado,
    fonte_dado,
    fonte_url,
    status
)
select
    s.nome,
    s.marca,
    s.tipo_cafe,
    s.torra,
    s.categoria,
    null,
    'catalogo_inicial',
    'catalogo_manual_inicial',
    'https://www.abic.com.br/certificacoes/',
    'ativo'
from seed s
where not exists (
    select 1
    from public.cafes c
    where lower(trim(c.nome)) = lower(trim(s.nome))
      and lower(trim(c.marca)) = lower(trim(s.marca))
);

select
    count(*) as total_cafes_ativos
from public.cafes
where coalesce(status, 'ativo') = 'ativo';

select
    cafe_id,
    nome,
    marca,
    tipo_cafe,
    torra,
    nota_media,
    total_avaliacoes,
    preco_kg_medio,
    preco_250g_medio
from public.cafes_resumo
order by
    total_avaliacoes desc,
    nome
limit 30;

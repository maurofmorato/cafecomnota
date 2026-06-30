-- ============================================================
-- Café com nota
-- Versão 1.0.7 - melhora modelo de preços e prepara catálogo inicial
-- Execute no Supabase SQL Editor.
-- ============================================================

-- 1) Garante colunas de origem/catálogo na tabela de cafés.
alter table public.cafes
    add column if not exists categoria text,
    add column if not exists certificacao text,
    add column if not exists origem_dado text default 'usuario',
    add column if not exists fonte_dado text,
    add column if not exists fonte_url text,
    add column if not exists updated_at timestamptz default now();

-- 2) Tabela separada para histórico de preços.
-- Assim o preço não fica "eterno": cada preço tem data.
create table if not exists public.precos_cafe (
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
    loja text,
    cidade text,
    uf char(2),
    origem_preco text not null default 'usuario',

    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint precos_cafe_preco_pago_positivo check (preco_pago > 0),
    constraint precos_cafe_peso_g_positivo check (peso_g > 0)
);

create index if not exists idx_precos_cafe_cafe_id
    on public.precos_cafe(cafe_id);

create index if not exists idx_precos_cafe_data_preco
    on public.precos_cafe(data_preco desc);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists trg_precos_cafe_updated_at on public.precos_cafe;

create trigger trg_precos_cafe_updated_at
before update on public.precos_cafe
for each row
execute function public.set_updated_at();

alter table public.precos_cafe enable row level security;

drop policy if exists "precos_cafe_leitura_publica" on public.precos_cafe;
create policy "precos_cafe_leitura_publica"
on public.precos_cafe
for select
using (true);

drop policy if exists "precos_cafe_insert_authenticated" on public.precos_cafe;
create policy "precos_cafe_insert_authenticated"
on public.precos_cafe
for insert
to authenticated
with check (
    auth.uid() = usuario_id
);

drop policy if exists "precos_cafe_update_owner" on public.precos_cafe;
create policy "precos_cafe_update_owner"
on public.precos_cafe
for update
to authenticated
using (
    auth.uid() = usuario_id
)
with check (
    auth.uid() = usuario_id
);

-- 3) Recria a view de resumo.
-- Ranking vem de avaliações.
-- Preço vem do histórico de preços e também de avaliações antigas que já tinham preço.
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
        )::integer as percentual_compraria_novamente
    from public.avaliacoes a
    group by a.cafe_id
),
precos_unificados as (
    select
        p.cafe_id,
        p.preco_kg,
        p.preco_250g,
        p.data_preco,
        p.created_at
    from public.precos_cafe p

    union all

    select
        a.cafe_id,
        a.preco_por_kg as preco_kg,
        round((a.preco_por_kg / 4.0)::numeric, 2) as preco_250g,
        a.created_at::date as data_preco,
        a.created_at
    from public.avaliacoes a
    where a.preco_por_kg is not null
),
precos_resumo as (
    select
        p.cafe_id,

        round(
            avg(p.preco_kg) filter (
                where p.data_preco >= current_date - interval '90 days'
            )::numeric,
            2
        ) as preco_kg_medio_90d,

        round(avg(p.preco_kg)::numeric, 2) as preco_kg_medio_geral,

        round(
            avg(p.preco_250g) filter (
                where p.data_preco >= current_date - interval '90 days'
            )::numeric,
            2
        ) as preco_250g_medio_90d,

        round(avg(p.preco_250g)::numeric, 2) as preco_250g_medio_geral,

        max(p.data_preco) as ultimo_preco_em,
        count(*)::integer as total_precos
    from precos_unificados p
    group by p.cafe_id
)
select
    c.id as cafe_id,
    c.nome,
    c.marca,
    c.tipo_cafe,
    c.torra,
    c.categoria,
    c.certificacao,
    c.origem_dado,
    c.fonte_dado,
    c.fonte_url,

    ar.nota_media,
    coalesce(ar.total_avaliacoes, 0) as total_avaliacoes,
    coalesce(ar.percentual_compraria_novamente, 0) as percentual_compraria_novamente,

    coalesce(pr.preco_kg_medio_90d, pr.preco_kg_medio_geral) as preco_kg_medio,
    coalesce(pr.preco_250g_medio_90d, pr.preco_250g_medio_geral) as preco_250g_medio,
    pr.ultimo_preco_em,
    coalesce(pr.total_precos, 0) as total_precos
from public.cafes c
left join avaliacoes_resumo ar
    on ar.cafe_id = c.id
left join precos_resumo pr
    on pr.cafe_id = c.id
where coalesce(c.status, 'ativo') = 'ativo';

grant select on public.cafes_resumo to anon, authenticated;
grant select on public.precos_cafe to anon, authenticated;
grant insert, update on public.precos_cafe to authenticated;

-- 4) Carga inicial de catálogo.
-- Importante:
-- - Não cria ranking.
-- - Não cria avaliação.
-- - Não inventa preço.
-- - Só deixa a base mais interessante para busca.
with seed(nome, marca, tipo_cafe, torra, categoria) as (
values
    ('3 Corações Tradicional', '3 Corações', 'Moído', 'Média', 'Tradicional'),
    ('3 Corações Extra Forte', '3 Corações', 'Moído', 'Escura', 'Extraforte'),
    ('3 Corações Gourmet', '3 Corações', 'Moído', 'Média', 'Gourmet'),
    ('3 Corações Rituais Cerrado Mineiro', '3 Corações', 'Moído', 'Média', 'Especial'),
    ('3 Corações Rituais Mogiana Paulista', '3 Corações', 'Moído', 'Média', 'Especial'),
    ('3 Corações Rituais Sul de Minas', '3 Corações', 'Moído', 'Média', 'Especial'),
    ('3 Corações Portinari', '3 Corações', 'Moído', 'Média', 'Gourmet'),
    ('Pilão Tradicional', 'Pilão', 'Moído', 'Média', 'Tradicional'),
    ('Pilão Extra Forte', 'Pilão', 'Moído', 'Escura', 'Extraforte'),
    ('Pilão Intenso', 'Pilão', 'Moído', 'Escura', 'Tradicional'),
    ('Pilão Cafeteria Espresso', 'Pilão', 'Grãos', 'Média', 'Gourmet'),
    ('Pilão Cafeteria Torrado e Moído', 'Pilão', 'Moído', 'Média', 'Superior'),
    ('Melitta Tradicional', 'Melitta', 'Moído', 'Média', 'Tradicional'),
    ('Melitta Extra Forte', 'Melitta', 'Moído', 'Escura', 'Extraforte'),
    ('Melitta Especial', 'Melitta', 'Moído', 'Média', 'Superior'),
    ('Melitta Regiões Brasileiras Sul de Minas', 'Melitta', 'Moído', 'Média', 'Especial'),
    ('Melitta Regiões Brasileiras Cerrado', 'Melitta', 'Moído', 'Média', 'Especial'),
    ('Melitta Regiões Brasileiras Mogiana', 'Melitta', 'Moído', 'Média', 'Especial'),
    ('Café do Ponto Tradicional', 'Café do Ponto', 'Moído', 'Média', 'Tradicional'),
    ('Café do Ponto Extra Forte', 'Café do Ponto', 'Moído', 'Escura', 'Extraforte'),
    ('Café do Ponto Exportação', 'Café do Ponto', 'Moído', 'Média', 'Superior'),
    ('Café do Ponto Safra Especial', 'Café do Ponto', 'Moído', 'Média', 'Especial'),
    ('Café Brasileiro Tradicional', 'Café Brasileiro', 'Moído', 'Média', 'Tradicional'),
    ('Café Brasileiro Extra Forte', 'Café Brasileiro', 'Moído', 'Escura', 'Extraforte'),
    ('Café Brasileiro Superior', 'Café Brasileiro', 'Moído', 'Média', 'Superior'),
    ('Caboclo Tradicional', 'Caboclo', 'Moído', 'Média', 'Tradicional'),
    ('Caboclo Extra Forte', 'Caboclo', 'Moído', 'Escura', 'Extraforte'),
    ('Café Pelé Tradicional', 'Café Pelé', 'Moído', 'Média', 'Tradicional'),
    ('Café Pelé Extra Forte', 'Café Pelé', 'Moído', 'Escura', 'Extraforte'),
    ('Café União Tradicional', 'Café União', 'Moído', 'Média', 'Tradicional'),
    ('Café União Extra Forte', 'Café União', 'Moído', 'Escura', 'Extraforte'),
    ('Santa Clara Tradicional', 'Santa Clara', 'Moído', 'Média', 'Tradicional'),
    ('Santa Clara Extra Forte', 'Santa Clara', 'Moído', 'Escura', 'Extraforte'),
    ('Santa Clara Reserva da Família', 'Santa Clara', 'Moído', 'Média', 'Superior'),
    ('Pimpinela Tradicional', 'Pimpinela', 'Moído', 'Média', 'Tradicional'),
    ('Pimpinela Extra Forte', 'Pimpinela', 'Moído', 'Escura', 'Extraforte'),
    ('Pimpinela Gourmet', 'Pimpinela', 'Moído', 'Média', 'Gourmet'),
    ('Orfeu Intenso', 'Orfeu', 'Grãos', 'Média', 'Especial'),
    ('Orfeu Clássico', 'Orfeu', 'Grãos', 'Média', 'Especial'),
    ('Orfeu Bourbon Amarelo', 'Orfeu', 'Grãos', 'Média', 'Especial'),
    ('Orfeu Orgânico', 'Orfeu', 'Grãos', 'Média', 'Especial'),
    ('Baggio Bourbon', 'Baggio', 'Moído', 'Média', 'Gourmet'),
    ('Baggio Chocolate Trufado', 'Baggio', 'Moído', 'Média', 'Aromatizado'),
    ('Baggio Aroma de Avelã', 'Baggio', 'Moído', 'Média', 'Aromatizado'),
    ('Baggio Gourmet', 'Baggio', 'Moído', 'Média', 'Gourmet'),
    ('Octavio Café Bourbon Amarelo', 'Octavio Café', 'Grãos', 'Média', 'Especial'),
    ('Octavio Café Blend', 'Octavio Café', 'Grãos', 'Média', 'Especial'),
    ('Santo Grão Blend', 'Santo Grão', 'Grãos', 'Média', 'Especial'),
    ('Coffee++ Frutado', 'Coffee++', 'Grãos', 'Média clara', 'Especial'),
    ('Coffee++ Chocolate', 'Coffee++', 'Grãos', 'Média', 'Especial'),
    ('Unique Cafés Frutado', 'Unique Cafés', 'Grãos', 'Média clara', 'Especial'),
    ('Unique Cafés Bourbon Amarelo', 'Unique Cafés', 'Grãos', 'Média', 'Especial'),
    ('Lucca Cafés Espresso', 'Lucca Cafés', 'Grãos', 'Média', 'Especial'),
    ('Lucca Cafés Clássico', 'Lucca Cafés', 'Grãos', 'Média', 'Especial'),
    ('Dutras Coffee Catuaí Vermelho', 'Dutras Coffee', 'Grãos', 'Média', 'Especial'),
    ('Fazenda Pessegueiro Especial', 'Fazenda Pessegueiro', 'Grãos', 'Média', 'Especial'),
    ('Fazenda Sertão Bourbon Amarelo', 'Fazenda Sertão', 'Grãos', 'Média', 'Especial'),
    ('Um Coffee Co. Espresso', 'Um Coffee Co.', 'Grãos', 'Média', 'Especial'),
    ('Isso é Café Blend', 'Isso é Café', 'Grãos', 'Média', 'Especial'),
    ('Black Tucano Coffee', 'Black Tucano', 'Grãos', 'Média', 'Especial'),
    ('Suplicy Espresso', 'Suplicy', 'Grãos', 'Média', 'Especial'),
    ('Starbucks Pike Place Roast', 'Starbucks', 'Moído', 'Média', 'Gourmet'),
    ('Starbucks Espresso Roast', 'Starbucks', 'Grãos', 'Escura', 'Gourmet')
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

-- 5) Conferências.
select
    count(*) as total_cafes_ativos
from public.cafes
where coalesce(status, 'ativo') = 'ativo';

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
order by
    total_avaliacoes desc,
    nome
limit 20;

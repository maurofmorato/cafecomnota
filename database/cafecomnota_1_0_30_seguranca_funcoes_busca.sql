-- Cafe com nota 1.0.30
-- Corrige a busca e restringe funcoes internas do Supabase.
--
-- Esta migracao foi aplicada e validada no Supabase em 2026-07-28.
-- Nao altera dados de cafes nem de usuarios.

begin;

-- A busca anterior tentava ler codigo_barras em cafes_resumo, mas essa
-- coluna nao existe na view. A comparacao passa a usar a tabela cafes,
-- que continua protegida pelas politicas RLS.
create or replace function public.buscar_cafes(p_termo text)
returns table (
    cafe_id uuid,
    nome text,
    marca text,
    tipo_cafe text,
    torra text,
    total_avaliacoes integer,
    nota_media numeric,
    preco_kg_medio numeric
)
language sql
stable
security invoker
set search_path = ''
as $function$
    select
        cr.cafe_id,
        cr.nome,
        cr.marca,
        cr.tipo_cafe,
        cr.torra,
        cr.total_avaliacoes,
        cr.nota_media,
        cr.preco_kg_medio
    from public.cafes_resumo cr
    join public.cafes c
      on c.id = cr.cafe_id
    where
        public.normalizar_texto(cr.nome) ilike
            '%' || public.normalizar_texto(p_termo) || '%'
        or public.normalizar_texto(cr.marca) ilike
            '%' || public.normalizar_texto(p_termo) || '%'
        or coalesce(c.codigo_barras, '') =
            pg_catalog.btrim(coalesce(p_termo, ''))
    order by
        cr.total_avaliacoes desc,
        cr.nota_media desc nulls last,
        cr.nome asc
    limit 30;
$function$;

-- Fixa o caminho de resolucao de objetos para evitar substituicao
-- maliciosa de funcoes ou operadores.
alter function public.normalizar_texto(text)
    set search_path = '';

alter function public.set_updated_at()
    set search_path = '';

alter function public.set_campos_busca_cafe()
    set search_path = '';

-- Funcoes de gatilho nao devem ser expostas como RPC pela Data API.
-- Os gatilhos continuam habilitados e funcionando dentro do banco.
revoke execute on function public.criar_usuario_publico_auth()
    from public, anon, authenticated, service_role;

revoke execute on function public.handle_new_user()
    from public, anon, authenticated, service_role;

revoke execute on function public.registrar_auditoria_moderacao_cafe()
    from public, anon, authenticated, service_role;

revoke execute on function public.set_updated_at()
    from public, anon, authenticated, service_role;

revoke execute on function public.set_campos_busca_cafe()
    from public, anon, authenticated, service_role;

commit;

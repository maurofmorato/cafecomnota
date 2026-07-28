-- Cafe com nota 1.0.30
-- Move a verificacao privilegiada de administrador para schema privado.
--
-- Esta migracao foi aplicada e validada no Supabase em 2026-07-28.
-- Mantem o mesmo endpoint RPC usado pelo aplicativo Android.

begin;

create schema if not exists private;

revoke all on schema private from public;
grant usage on schema private to anon, authenticated, service_role;

-- Move a funcao SECURITY DEFINER existente. As politicas RLS mantem a
-- dependencia pelo identificador interno e passam a referencia-la no
-- schema private automaticamente.
alter function public.usuario_atual_is_admin()
    set schema private;

revoke all on function private.usuario_atual_is_admin()
    from public, anon, authenticated, service_role;

grant execute on function private.usuario_atual_is_admin()
    to anon, authenticated, service_role;

-- Preserva /rest/v1/rpc/usuario_atual_is_admin para o aplicativo.
-- A funcao publica nao possui privilegios elevados; ela apenas encaminha
-- a verificacao para a implementacao privada.
create function public.usuario_atual_is_admin()
returns boolean
language sql
stable
security invoker
set search_path = ''
as $function$
    select private.usuario_atual_is_admin();
$function$;

revoke all on function public.usuario_atual_is_admin()
    from public, anon, service_role;

grant execute on function public.usuario_atual_is_admin()
    to authenticated;

commit;

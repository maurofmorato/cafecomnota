-- ============================================================
-- Café com nota
-- Versão 1.0.22 - até cinco fotos de rótulo por café
-- Execute uma única vez no SQL Editor do Supabase.
-- ============================================================

begin;

-- Bucket privado: as fotos pendentes ficam visíveis somente ao autor e aos administradores.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'cafe-rotulos',
    'cafe-rotulos',
    false,
    5242880,
    array['image/jpeg']
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

create table if not exists public.cafe_fotos (
    id uuid primary key default gen_random_uuid(),
    cafe_id uuid not null references public.cafes(id) on delete cascade,
    storage_path text not null unique,
    rotulo text not null,
    ordem smallint not null,
    enviada_por uuid not null references public.usuarios(id) on delete cascade,
    criada_em timestamptz not null default now(),
    constraint cafe_fotos_rotulo_check check (
        rotulo in ('frente', 'verso', 'lateral', 'informacoes', 'codigo_barras', 'outra')
    ),
    constraint cafe_fotos_ordem_check check (ordem between 0 and 4),
    constraint cafe_fotos_cafe_ordem_unique unique (cafe_id, ordem)
);

create index if not exists cafe_fotos_cafe_id_ordem_idx
    on public.cafe_fotos (cafe_id, ordem);

-- A primeira foto é a capa, identificada por ordem = 0.
create or replace function public.limitar_cinco_fotos_por_cafe()
returns trigger
language plpgsql
set search_path = public
as $$
begin
    perform 1 from public.cafes where id = new.cafe_id for update;

    if (select count(*) from public.cafe_fotos where cafe_id = new.cafe_id) >= 5 then
        raise exception 'Cada café pode ter no máximo cinco fotos.';
    end if;

    return new;
end;
$$;

drop trigger if exists cafe_fotos_limitar_cinco on public.cafe_fotos;
create trigger cafe_fotos_limitar_cinco
before insert on public.cafe_fotos
for each row
execute function public.limitar_cinco_fotos_por_cafe();

alter table public.cafe_fotos enable row level security;

drop policy if exists "cafe_fotos_select_publicadas_autor_ou_admin" on public.cafe_fotos;
create policy "cafe_fotos_select_publicadas_autor_ou_admin"
on public.cafe_fotos
for select
to anon, authenticated
using (
    enviada_por = (select auth.uid())
    or public.usuario_atual_is_admin()
    or exists (
        select 1
        from public.cafes c
        where c.id = cafe_fotos.cafe_id
          and c.status = 'ativo'
    )
);

drop policy if exists "cafe_fotos_insert_pelo_autor_ou_admin" on public.cafe_fotos;
create policy "cafe_fotos_insert_pelo_autor_ou_admin"
on public.cafe_fotos
for insert
to authenticated
with check (
    enviada_por = (select auth.uid())
    and (
        public.usuario_atual_is_admin()
        or exists (
            select 1
            from public.cafes c
            where c.id = cafe_fotos.cafe_id
              and c.cadastrado_por = (select auth.uid())
        )
    )
);

drop policy if exists "cafe_fotos_delete_pelo_autor_pendente_ou_admin" on public.cafe_fotos;
create policy "cafe_fotos_delete_pelo_autor_pendente_ou_admin"
on public.cafe_fotos
for delete
to authenticated
using (
    public.usuario_atual_is_admin()
    or (
        enviada_por = (select auth.uid())
        and exists (
            select 1
            from public.cafes c
            where c.id = cafe_fotos.cafe_id
              and c.status = 'pendente'
        )
    )
);

grant select on public.cafe_fotos to anon, authenticated;
grant insert, delete on public.cafe_fotos to authenticated;

-- Arquivos: autor e administrador veem os pendentes; fotos de cafés ativos ficam disponíveis ao app.
drop policy if exists "cafe_rotulos_select_conforme_cafe" on storage.objects;
create policy "cafe_rotulos_select_conforme_cafe"
on storage.objects
for select
to anon, authenticated
using (
    bucket_id = 'cafe-rotulos'
    and (
        owner_id = (select auth.jwt()->>'sub')
        or public.usuario_atual_is_admin()
        or exists (
            select 1
            from public.cafe_fotos f
            join public.cafes c on c.id = f.cafe_id
            where f.storage_path = storage.objects.name
              and c.status = 'ativo'
        )
    )
);

drop policy if exists "cafe_rotulos_insert_pelo_proprio_usuario" on storage.objects;
create policy "cafe_rotulos_insert_pelo_proprio_usuario"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'cafe-rotulos'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
);

drop policy if exists "cafe_rotulos_delete_pelo_proprio_usuario_ou_admin" on storage.objects;
create policy "cafe_rotulos_delete_pelo_proprio_usuario_ou_admin"
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'cafe-rotulos'
    and (
        owner_id = (select auth.jwt()->>'sub')
        or public.usuario_atual_is_admin()
    )
);

notify pgrst, 'reload schema';

commit;

-- Conferência após executar:
-- select id, name, public, file_size_limit from storage.buckets where id = 'cafe-rotulos';
-- select cafe_id, rotulo, ordem, storage_path, enviada_por from public.cafe_fotos order by criada_em desc;

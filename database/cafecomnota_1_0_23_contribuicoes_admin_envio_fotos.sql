-- ============================================================
-- Café com nota — versão 1.0.23
-- Cadastro recuperável de fotos, contribuições e auditoria.
-- Execute uma única vez no SQL Editor do Supabase.
-- ============================================================

begin;

alter table public.cafes
    add column if not exists chave_envio uuid,
    add column if not exists fotos_esperadas smallint not null default 0,
    add column if not exists fotos_enviadas smallint not null default 0,
    add column if not exists fotos_status text not null default 'nao_solicitada';

alter table public.cafes
    drop constraint if exists cafes_fotos_status_check;

alter table public.cafes
    add constraint cafes_fotos_status_check check (
        fotos_status in ('nao_solicitada', 'pendente', 'enviando', 'concluida', 'falhou')
    );

alter table public.cafes
    add constraint cafes_fotos_quantidade_check check (
        fotos_esperadas between 0 and 5
        and fotos_enviadas between 0 and 5
        and fotos_enviadas <= fotos_esperadas
    );

create unique index if not exists cafes_cadastrado_por_chave_envio_unica
    on public.cafes (cadastrado_por, chave_envio)
    where chave_envio is not null;

-- O autor somente ajusta metadados de envio enquanto o café ainda está pendente.
drop policy if exists "cafes_autor_atualiza_envio_pendente" on public.cafes;
create policy "cafes_autor_atualiza_envio_pendente"
on public.cafes
for update
to authenticated
using (
    cadastrado_por = (select auth.uid())
    and status = 'pendente'
)
with check (
    cadastrado_por = (select auth.uid())
    and status = 'pendente'
);

drop policy if exists "cafe_fotos_update_pelo_autor_pendente_ou_admin" on public.cafe_fotos;
create policy "cafe_fotos_update_pelo_autor_pendente_ou_admin"
on public.cafe_fotos
for update
to authenticated
using (
    public.usuario_atual_is_admin()
    or (
        enviada_por = (select auth.uid())
        and exists (
            select 1 from public.cafes c
            where c.id = cafe_fotos.cafe_id and c.status = 'pendente'
        )
    )
)
with check (
    public.usuario_atual_is_admin()
    or enviada_por = (select auth.uid())
);

grant update on public.cafes, public.cafe_fotos to authenticated;

-- Upsert do arquivo no mesmo caminho quando o celular retoma um envio.
drop policy if exists "cafe_rotulos_update_pelo_proprio_usuario_ou_admin" on storage.objects;
create policy "cafe_rotulos_update_pelo_proprio_usuario_ou_admin"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'cafe-rotulos'
    and (
        owner_id = (select auth.jwt()->>'sub')
        or public.usuario_atual_is_admin()
    )
)
with check (
    bucket_id = 'cafe-rotulos'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
);

create table if not exists public.cafe_auditoria (
    id uuid primary key default gen_random_uuid(),
    cafe_id uuid not null references public.cafes(id) on delete cascade,
    acao text not null,
    status_anterior text,
    status_novo text,
    motivo text,
    realizado_por uuid references public.usuarios(id) on delete set null,
    criado_em timestamptz not null default now()
);

create index if not exists cafe_auditoria_cafe_data_idx
    on public.cafe_auditoria (cafe_id, criado_em desc);

alter table public.cafe_auditoria enable row level security;
drop policy if exists "cafe_auditoria_somente_admin" on public.cafe_auditoria;
create policy "cafe_auditoria_somente_admin"
on public.cafe_auditoria
for select
to authenticated
using (public.usuario_atual_is_admin());
grant select on public.cafe_auditoria to authenticated;

create or replace function public.registrar_auditoria_moderacao_cafe()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if public.usuario_atual_is_admin()
       and (
           old.status is distinct from new.status
           or old.nome is distinct from new.nome
           or old.marca is distinct from new.marca
           or old.tipo_cafe is distinct from new.tipo_cafe
           or old.torra is distinct from new.torra
           or old.peso_padrao_g is distinct from new.peso_padrao_g
       ) then
        insert into public.cafe_auditoria (
            cafe_id, acao, status_anterior, status_novo, motivo, realizado_por
        ) values (
            new.id,
            case when old.status is distinct from new.status then 'moderacao' else 'edicao' end,
            old.status,
            new.status,
            new.motivo_moderacao,
            auth.uid()
        );
    end if;
    return new;
end;
$$;

drop trigger if exists cafes_registrar_auditoria_moderacao on public.cafes;
create trigger cafes_registrar_auditoria_moderacao
after update on public.cafes
for each row execute function public.registrar_auditoria_moderacao_cafe();

notify pgrst, 'reload schema';
commit;

-- Conferência:
-- select nome, status, fotos_status, fotos_enviadas, fotos_esperadas from public.cafes order by cadastrado_em desc;
-- select * from public.cafe_auditoria order by criado_em desc;

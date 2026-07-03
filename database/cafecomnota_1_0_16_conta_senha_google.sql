-- ============================================================
-- Café com nota
-- Versão 1.0.16 - conta, senha e Google Auth
-- ============================================================

begin;

-- Garante que usuários criados por Google Auth também ganhem perfil público.
alter table public.usuarios
    add column if not exists is_admin boolean not null default false;

create or replace function public.criar_usuario_publico_auth()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.usuarios (
        id,
        apelido,
        nome_exibicao,
        avatar_url,
        ativo
    )
    values (
        new.id,
        coalesce(
            nullif(new.raw_user_meta_data->>'name', ''),
            nullif(new.raw_user_meta_data->>'full_name', ''),
            split_part(new.email, '@', 1),
            'usuario'
        ),
        coalesce(
            nullif(new.raw_user_meta_data->>'name', ''),
            nullif(new.raw_user_meta_data->>'full_name', ''),
            split_part(new.email, '@', 1),
            'Usuário'
        ),
        coalesce(
            nullif(new.raw_user_meta_data->>'avatar_url', ''),
            nullif(new.raw_user_meta_data->>'picture', '')
        ),
        true
    )
    on conflict (id) do update
    set
        nome_exibicao = coalesce(
            nullif(excluded.nome_exibicao, ''),
            public.usuarios.nome_exibicao
        ),
        avatar_url = coalesce(
            excluded.avatar_url,
            public.usuarios.avatar_url
        ),
        ativo = coalesce(public.usuarios.ativo, true),
        updated_at = now();

    return new;
end;
$$;

drop trigger if exists on_auth_user_created_criar_usuario_publico on auth.users;

create trigger on_auth_user_created_criar_usuario_publico
after insert on auth.users
for each row
execute function public.criar_usuario_publico_auth();

-- Completa perfis públicos que ainda não existem.
insert into public.usuarios (
    id,
    apelido,
    nome_exibicao,
    avatar_url,
    ativo
)
select
    au.id,
    coalesce(
        nullif(au.raw_user_meta_data->>'name', ''),
        nullif(au.raw_user_meta_data->>'full_name', ''),
        split_part(au.email, '@', 1),
        'usuario'
    ),
    coalesce(
        nullif(au.raw_user_meta_data->>'name', ''),
        nullif(au.raw_user_meta_data->>'full_name', ''),
        split_part(au.email, '@', 1),
        'Usuário'
    ),
    coalesce(
        nullif(au.raw_user_meta_data->>'avatar_url', ''),
        nullif(au.raw_user_meta_data->>'picture', '')
    ),
    true
from auth.users au
left join public.usuarios u
    on u.id = au.id
where u.id is null;

notify pgrst, 'reload schema';

commit;

-- Conferência
select
    au.id,
    au.email,
    u.apelido,
    u.nome_exibicao,
    u.avatar_url,
    u.is_admin,
    u.ativo
from auth.users au
left join public.usuarios u
    on u.id = au.id
order by au.created_at desc;

-- ============================================================
-- Café com nota
-- Versão 1.0.24 - administração em lista e remoção administrativa
-- Execute uma única vez no SQL Editor do Supabase.
-- ============================================================

begin;

-- "removido" significa: fora da busca, do ranking e das telas públicas.
-- O registro continua no banco exclusivamente para auditoria administrativa;
-- não é uma exclusão física que poderia apagar evidências ou fotos sem revisão.
alter table public.cafes
    drop constraint if exists cafes_status_chk;

alter table public.cafes
    drop constraint if exists cafes_status_check;

alter table public.cafes
    add constraint cafes_status_check
    check (status in ('ativo', 'pendente', 'oculto', 'rejeitado', 'removido'));

alter table public.cafes
    add column if not exists removido_por uuid references public.usuarios(id) on delete set null,
    add column if not exists removido_em timestamptz;

-- Registra quem efetuou a remoção. O UPDATE continua protegido pela política
-- administrativa já existente (cafes_admin_update).
create or replace function public.preencher_remocao_cafe()
returns trigger
language plpgsql
set search_path = public
as $$
begin
    if new.status = 'removido' and old.status is distinct from 'removido' then
        new.removido_por := auth.uid();
        new.removido_em := now();
    elsif new.status is distinct from 'removido' and old.status = 'removido' then
        new.removido_por := null;
        new.removido_em := null;
    end if;

    return new;
end;
$$;

drop trigger if exists before_cafes_status_remocao on public.cafes;
create trigger before_cafes_status_remocao
before update of status on public.cafes
for each row
execute function public.preencher_remocao_cafe();

-- A lista da administração é ordenada por data e filtrada por status.
create index if not exists cafes_status_cadastrado_em_idx
    on public.cafes (status, cadastrado_em desc);

notify pgrst, 'reload schema';

commit;

-- Verificação esperada após executar:
-- select status, count(*) from public.cafes group by status order by status;

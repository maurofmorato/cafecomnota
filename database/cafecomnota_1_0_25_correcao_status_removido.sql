-- Café com nota - correção definitiva da regra de status.
-- Execute uma única vez no SQL Editor do Supabase.
-- Trata os dois nomes usados historicamente para a restrição.

begin;

alter table public.cafes
    drop constraint if exists cafes_status_chk;

alter table public.cafes
    drop constraint if exists cafes_status_check;

alter table public.cafes
    add constraint cafes_status_check
    check (status in ('ativo', 'pendente', 'oculto', 'rejeitado', 'removido'));

notify pgrst, 'reload schema';

commit;

-- Conferência:
-- select conname, pg_get_constraintdef(oid)
-- from pg_constraint
-- where conrelid = 'public.cafes'::regclass
--   and contype = 'c'
--   and conname like 'cafes_status%';

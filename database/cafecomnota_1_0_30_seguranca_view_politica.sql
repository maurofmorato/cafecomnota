-- Cafe com nota 1.0.30
-- Endurece a leitura publica e remove uma politica legada permissiva.
--
-- Esta migracao foi aplicada e validada no Supabase em 2026-07-28.
-- Nao altera dados de cafes nem de usuarios.

begin;

-- Faz a view respeitar as permissoes e politicas RLS do usuario
-- que executa a consulta, evitando o comportamento SECURITY DEFINER.
alter view public.cafes_resumo
    set (security_invoker = on);

-- Politica legada baseada em criado_por. Essa coluna nao e usada pelos
-- cadastros atuais, que utilizam cadastrado_por e politicas especificas
-- para envios pendentes e administradores.
drop policy if exists cafes_update_creator
    on public.cafes;

commit;

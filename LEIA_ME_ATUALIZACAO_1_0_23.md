# Café com nota — atualização 1.0.23

## O que muda

- Fotos do rótulo podem ser adicionadas pela **câmera** ou pela **galeria**.
- O envio mostra o progresso, por exemplo: `3/5 fotos enviadas`.
- Se a conexão falhar, tocar em **Salvar café** novamente retoma somente as fotos pendentes; o café não é duplicado.
- Fotos temporárias feitas pela câmera são removidas do cache depois que o envio termina.
- O Perfil agora tem **Minhas contribuições**, para acompanhar cafés pendentes, publicados ou com envio de fotos incompleto.
- Administradores têm uma tela de **Administração de cafés** para corrigir nome/marca e aprovar, ocultar ou rejeitar com motivo. As decisões ficam registradas em auditoria.

## Banco de dados — obrigatório antes de testar o cadastro com fotos

No Supabase, abra **SQL Editor**, crie uma nova consulta, cole todo o conteúdo de:

`database/cafecomnota_1_0_23_contribuicoes_admin_envio_fotos.sql`

e execute uma única vez.

Essa migração não apaga cafés, avaliações nem fotos existentes. Ela adiciona o controle de envio recuperável, as permissões de atualização de fotos e a tabela de auditoria.

## Teste sugerido

1. Entre no app e cadastre um café com duas ou mais fotos.
2. Confirme que aparece o progresso do envio.
3. Desative a internet durante o envio de uma foto; a tela deve informar quantas foram concluídas.
4. Ative a internet e toque novamente em **Salvar café**. O mesmo café deve continuar o envio, sem criar outro cadastro.
5. No Perfil, abra **Minhas contribuições** e confira o status.
6. Com a conta de administrador, abra **Administração** e altere o status de um café de teste; confira `public.cafe_auditoria` no Supabase.

## Versão

- `versionCode = 18`
- `versionName = 1.0.23`

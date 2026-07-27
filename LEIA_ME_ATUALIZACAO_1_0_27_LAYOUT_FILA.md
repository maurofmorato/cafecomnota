# Café com nota — 1.0.27

## O que mudou

- Fila de revisão reorganizada: filtros por status e cartões compactos.
- Cada café agora é analisado em uma tela própria, sem despejar todos os campos na lista.
- Ações rápidas para aprovar ou abrir a revisão completa.
- Capa neutra de pacote para cafés sem imagem de produto. O texto do pacote acompanha o idioma selecionado no aplicativo.
- Revisão com sugestões de motivo, decisão clara e remoção com auditoria.
- Perfil conectado mais compacto: a alteração de senha abre somente quando for solicitada.

## Banco de dados

Esta atualização não exige SQL novo. Ela usa os status já adotados:

`pendente`, `ativo`, `oculto`, `rejeitado` e `removido`.

## Antes de publicar

1. Compile o APK de depuração e teste a Fila de revisão.
2. Verifique um café sem fotos: ele deve exibir a capa-padrão, sem imagem quebrada.
3. Verifique a remoção: o café deve deixar a fila e as telas públicas, permanecendo apenas no filtro **Removidos** e na auditoria.
4. Gere o AAB de release somente depois de concluir o teste local.

## Observação

Fotos reais de produtos continuarão sendo usadas quando forem disponibilizadas pela base. A capa-padrão é o plano B elegante, não substitui uma boa foto do café.

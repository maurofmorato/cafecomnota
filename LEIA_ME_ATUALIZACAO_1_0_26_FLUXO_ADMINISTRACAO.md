# Café com nota — atualização 1.0.26

## O que mudou

- Administração agora abre como uma fila de revisão, priorizando cafés pendentes.
- Cada filtro mostra sua quantidade e se ajusta à largura da tela.
- Cafés pendentes podem ser aprovados diretamente na lista.
- A revisão completa ganhou atalhos de motivo: possível duplicata, fotos insuficientes, dados incompletos e produto não encontrado.
- Erros de operação agora aparecem em linguagem amigável, sem exibir mensagens internas do banco.
- A tela Perfil organiza melhor as ferramentas de moderador, a atividade do usuário, preferências e privacidade.
- Versão: `1.0.26` (`versionCode 21`).

## Teste sugerido

1. Entre com a conta administradora.
2. Abra **Perfil > Ferramentas de moderador > Abrir fila de revisão**.
3. Confira se a fila inicia em **Pendente** e se os filtros não ficam cortados.
4. Aprove um café pendente pela própria lista.
5. Abra outro café em **Revisar**, teste os atalhos de motivo e salve uma alteração.
6. Remova um café de teste e confirme que ele some de **Todos**, mas aparece no filtro **Removido**.

## Segurança do pacote

Este pacote não inclui `keystore.properties`, `local.properties`, chaves, builds, APKs/AABs, `.gradle` ou configurações locais da IDE.

# Café com nota 1.0.20 — correção da leitura de rótulos (build 15)

## O que mudou

- `versionCode` atualizado para `15`; o `versionName` permanece `1.0.20`;
- a leitura compara o texto reconhecido com os cafés já existentes no catálogo;
- textos do navegador e frases genéricas como “Café com...” deixam de ser usados como marca;
- descritores como “Chocolate Trufado”, “Bourbon” e “Extra Forte” ganham prioridade;
- uma palavra “forte” isolada não é mais interpretada automaticamente como torra escura;
- cada nova foto limpa sugestões antigas, evitando reaproveitar tipo ou torra de outra embalagem;
- tipo e torra não reconhecidos ficam sem seleção e precisam ser confirmados pelo usuário;
- o QR provisório do teste fechado foi atualizado para o build `15`.

## Resultado esperado para o rótulo Baggio do teste

- nome: `Baggio Chocolate Trufado`;
- marca: `Baggio`;
- peso: `250 g`;
- tipo: `Moído`;
- torra: `Média`, quando o produto for encontrado no catálogo existente.

Se o produto não estiver no catálogo e a torra não constar no rótulo, o app não inventará a informação.

## Observação sobre a foto

Fotografar a tela de outro celular reduz a qualidade por causa de reflexos, pixels da tela e textos do navegador. Sempre que possível, fotografe diretamente a embalagem, com o rótulo ocupando a maior parte da imagem.

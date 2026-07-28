# Café com nota — atualização 1.0.18

Este pacote contém somente fontes e configuração pública. Ele não inclui senhas, chaves, `keystore.properties`, arquivos `.jks`, `local.properties`, `google-services.json`, `.gradle`, `build` nem `.idea`.

## Novidades

- QR Code no detalhe do café, com opções para copiar e compartilhar o link.
- Abertura de links `cafecomnota://coffee/<id>` diretamente no detalhe do café.
- Retorno pelo botão físico do Android para a lista de origem (Ranking ou Busca).
- Aviso amigável de sessão expirada, direcionando o usuário ao Perfil.
- Botão **Ir para Perfil** nas telas que exigem login.
- Leitura inicial de rótulo pela câmera: a imagem é analisada no próprio aparelho para sugerir nome, marca e peso. Nenhuma foto é enviada ou armazenada pelo app.
- Nova Home com ilustrações originais nos cartões de Ranking, Avaliação e Cadastro, além da versão visível no selo de dados.
- Versão do app: **1.0.18** (`versionCode 12`).

## Como aplicar no Windows

1. Feche o Android Studio.
2. Faça uma cópia de segurança da pasta `D:\projetos\Cafecomnota`.
3. Extraia este ZIP diretamente em `D:\projetos\Cafecomnota`, permitindo substituir os arquivos.
4. Abra o projeto no Android Studio e aguarde a sincronização do Gradle.
5. Execute:

```powershell
cd D:\projetos\Cafecomnota
.\gradlew.bat clean bundleRelease
```

O AAB para enviar ao teste fechado estará em:

```text
app\build\outputs\bundle\release\app-release.aab
```

## Teste recomendado

1. Abra um café pelo Ranking e toque no botão físico **Voltar**: deve retornar ao Ranking.
2. Abra **Cadastrar café** sem sessão: toque em **Ir para Perfil**.
3. Faça login, aguarde a sessão expirar e tente salvar: o app deve voltar ao Perfil sem mostrar `JWT expired`.
4. Em um detalhe, toque no ícone de QR Code, copie o link e abra-o em um aparelho com o app instalado.
5. Em **Cadastrar café**, toque em **Fotografar rótulo** e confira as sugestões antes de salvar.

## Observação sobre o QR Code

Nesta etapa, o QR Code abre o app por link próprio (`cafecomnota://`). Para funcionar, quem recebe precisa ter o Café com nota instalado. Uma página pública por café (link HTTPS) pode ser a próxima evolução.

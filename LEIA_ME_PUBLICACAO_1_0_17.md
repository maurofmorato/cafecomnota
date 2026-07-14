# Café com nota 1.0.17 — preparação para Google Play

Este pacote atualiza a versão para `1.0.17` / `versionCode 11` e prepara os requisitos de privacidade para a publicação.

## Alterações

- Ícone do aplicativo declarado no AndroidManifest.
- Perfil com Política de Privacidade e solicitação de exclusão de conta.
- Página pública com Política de Privacidade e solicitação por e-mail.
- Texto de conta atualizado para refletir os recursos já disponíveis.

## Publicar a página de privacidade

Depois de aplicar este ZIP, envie a pasta `docs` ao GitHub:

```powershell
cd D:\projetos\Cafecomnota
git add app docs
git commit -m "Prepara privacidade e exclusão de conta para Play 1.0.17"
git push
```

No GitHub, abra o repositório `maurofmorato/cafecomnota` e acesse:

`Settings` → `Pages` → `Build and deployment`

Escolha:

- Source: `Deploy from a branch`
- Branch: `main`
- Folder: `/docs`

Salve. Depois que o GitHub informar a publicação, confirme que esta página abre no navegador:

`https://maurofmorato.github.io/cafecomnota/`

## Testar o aplicativo

```powershell
cd D:\temp\cafecomnota
Expand-Archive -Path .\cafecomnota_1_0_17_publicacao.zip -DestinationPath D:\projetos\Cafecomnota -Force

cd D:\projetos\Cafecomnota
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

No Perfil, teste os botões `Ler Política de Privacidade` e `Solicitar exclusão da conta`. Eles devem abrir a página publicada no GitHub Pages.

## Próximo passo

Depois da validação, gere o AAB de release assinado e envie primeiro para o teste interno da Google Play.

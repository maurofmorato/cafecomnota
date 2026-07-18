# Café com nota 1.0.19

Versão Android: `1.0.19`  
Version code: `13`

## Melhorias incluídas

- câmera usa uma foto em resolução completa para ler o rótulo;
- OCR sugere nome, marca, peso, tipo e torra, sempre com conferência do usuário;
- atualização flexível pela Google Play: o download ocorre em segundo plano e o app pede confirmação para reiniciar;
- botão físico **Voltar** retorna à tela anterior em todas as subtelas;
- novo cabeçalho visual nas telas de busca, ranking, perfil, detalhes, avaliação e cadastro;
- QR Code de compartilhamento permanece disponível nos detalhes do café.

## Aplicar os arquivos

Antes de substituir qualquer arquivo, execute o backup descrito em `BACKUP_E_CONTINUIDADE.md`.

No PowerShell:

```powershell
cd D:\temp\cafecomnota

Expand-Archive `
  -Path .\cafecomnota_1_0_19_camera_update_visual.zip `
  -DestinationPath D:\projetos\Cafecomnota `
  -Force

cd D:\projetos\Cafecomnota

Select-String `
  -Path .\app\build.gradle.kts `
  -Pattern 'versionCode|versionName'
```

O resultado esperado é `versionCode = 13` e `versionName = "1.0.19"`.

## Testar no emulador

```powershell
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) { throw "A compilação falhou. Não instale o APK anterior." }

adb devices
adb -s emulator-5554 install -r .\app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 shell am force-stop com.maurofmorato.cafecomnota
adb -s emulator-5554 shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

Para avaliar a câmera e o OCR, prefira também um teste no celular real, com o pacote apoiado, boa luz e o rótulo ocupando a maior parte da imagem.

## Gerar o App Bundle assinado

Mantenha `keystore.properties` e a chave de assinatura apenas no computador local. Depois execute:

```powershell
.\gradlew.bat clean bundleRelease
if ($LASTEXITCODE -ne 0) { throw "A geração do bundle falhou." }

Get-FileHash `
  .\app\build\outputs\bundle\release\app-release.aab `
  -Algorithm SHA256
```

O arquivo para a Play Console será:

`app\build\outputs\bundle\release\app-release.aab`

## Observação sobre atualização no próprio app

O fluxo de atualização só aparece quando o app foi instalado pela Google Play e existe uma versão superior disponível na faixa daquele usuário. Ele não é acionado em uma instalação feita por `adb`.

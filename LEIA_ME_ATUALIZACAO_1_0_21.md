# Café com nota 1.0.21 — rótulo completo e navegação intuitiva

Versão Android: `1.0.21`  
Código da versão: `16`

## Novidades

- captura separada da frente e do verso da embalagem;
- miniaturas permitem conferir ou refazer cada fotografia;
- as duas leituras são combinadas antes de sugerir os campos;
- leitura de nome, marca, peso, tipo, torra e dados opcionais da ficha técnica;
- confirmação manual continua obrigatória antes do envio;
- aviso destacado `Role para ver mais` nas telas com conteúdo abaixo da área visível;
- o aviso desaparece assim que a pessoa começa a rolar.

## Dados opcionais aproveitados do rótulo

- aromas e sabores declarados;
- origem ou região;
- produtor ou fazenda;
- variedade;
- processo;
- altitude;
- certificação.

Esses dados são descrições declaradas na embalagem. As notas numéricas de aroma,
sabor, corpo, acidez, amargor e doçura continuam sendo calculadas a partir das
avaliações da comunidade.

## Notas curtas para o Google Play

Fotografe a frente e o verso do pacote para obter sugestões mais completas. A leitura de rótulos foi aprimorada e as telas longas agora indicam quando há mais conteúdo abaixo.

## Teste local no Windows

```powershell
cd D:\projetos\Cafecomnota

& .\gradlew.bat testDebugUnitTest assembleDebug
$BuildOk = ($LASTEXITCODE -eq 0)

if (-not $BuildOk) {
    throw "A compilação falhou. Não instale o APK anterior."
}

adb devices
adb -s emulator-5554 install -r .\app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 shell am force-stop com.maurofmorato.cafecomnota
adb -s emulator-5554 shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

## Bundle para o teste fechado

```powershell
cd D:\projetos\Cafecomnota

& .\gradlew.bat clean bundleRelease
$BuildOk = ($LASTEXITCODE -eq 0)

if (-not $BuildOk) {
    throw "A compilação do bundle falhou. Não envie um AAB anterior."
}

Get-FileHash .\app\build\outputs\bundle\release\app-release.aab -Algorithm SHA256
```

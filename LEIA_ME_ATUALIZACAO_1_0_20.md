# Café com nota 1.0.20 — bloco de autenticação e compartilhamento

Versão Android: `1.0.20`  
Version code: `14`

## Incluído neste bloco

- a Home mostra somente `Versão 1.0.20`, sem mensagem técnica sobre Supabase;
- remoção da ActionBar/faixa preta e aplicação das cores claras nas barras do sistema;
- QR Code pequeno na Home para compartilhar o link provisório do teste fechado;
- QR grande com ações **Copiar** e **Compartilhar**;
- aviso de autenticação antes de abrir cadastro ou avaliação;
- retorno automático à ação desejada depois do login;
- preservação de dados digitados quando for necessário autenticar novamente;
- identificação de café semelhante durante o cadastro, com atalho para abrir o existente;
- melhorias anteriores da 1.0.19: câmera/OCR, atualização pela Play, botão Voltar e visual das subtelas.

## Link provisório usado no QR Code

`https://play.google.com/apps/test/com.maurofmorato.cafecomnota/14`

Antes da produção, esse endereço deverá ser substituído pelo endereço público definitivo da Play Store.

## Aplicar

Antes de tudo, faça o backup remoto da versão atual:

```powershell
cd D:\projetos\Cafecomnota
git status
git add app\build.gradle.kts app\src\main
git status
git commit -m "Backup antes da versao 1.0.20"
git push
```

Depois aplique o ZIP:

```powershell
cd D:\temp\cafecomnota

Expand-Archive `
  -Path .\cafecomnota_1_0_20_login_qr_visual.zip `
  -DestinationPath D:\projetos\Cafecomnota `
  -Force

cd D:\projetos\Cafecomnota

Select-String `
  -Path .\app\build.gradle.kts `
  -Pattern 'versionCode|versionName'
```

O esperado é `versionCode = 14` e `versionName = "1.0.20"`.

## Compilar e instalar no emulador

Execute o Gradle e o teste de resultado no mesmo bloco:

```powershell
& .\gradlew.bat assembleDebug
$BuildOk = ($LASTEXITCODE -eq 0)

if (-not $BuildOk) {
    throw "A compilação falhou. Não instale o APK anterior."
}

Write-Host "Compilação concluída. Instalando APK novo..." -ForegroundColor Green

adb -s emulator-5554 install -r .\app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 shell am force-stop com.maurofmorato.cafecomnota
adb -s emulator-5554 shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

## Testes principais

1. confirmar que a faixa preta desapareceu;
2. conferir `Versão 1.0.20` na Home;
3. abrir o QR pequeno, copiar e compartilhar o link;
4. sair da conta e tocar em **Cadastrar café novo**;
5. escolher **Entrar agora**, autenticar e confirmar a abertura automática do cadastro;
6. repetir com **Dar nota**;
7. digitar um café já existente e conferir o alerta de possível duplicidade;
8. usar o botão físico Voltar nas subtelas.

## Backup remoto depois dos testes

Quando tudo estiver aprovado:

```powershell
git status
git add app\build.gradle.kts
git add app\src\main
git add LEIA_ME_ATUALIZACAO_1_0_20.md
git commit -m "Implementa login antecipado QR e visual da versao 1.0.20"
git push
git status
```

Nunca adicione ao Git `keystore.properties`, chaves `.jks`, `local.properties` ou credenciais.

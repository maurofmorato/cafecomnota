# Café com nota - Versão 1.0.16

## Objetivo

Adicionar recursos de conta antes de enviar para testes/publicação:

1. Alterar senha dentro do app.
2. Esqueci minha senha com e-mail de recuperação.
3. Botão Entrar com Google.
4. Deep link para retorno do Supabase Auth.
5. Preparação do perfil público para usuários criados pelo Google.

## Como aplicar

Salve este ZIP em:

D:\temp\cafecomnota\

Execute:

cd D:\temp\cafecomnota

Expand-Archive -Path .\cafecomnota_1_0_16_conta_senha_google_20260703.zip -DestinationPath D:\projetos\Cafecomnota -Force

## SQL

Execute no Supabase SQL Editor:

D:\projetos\Cafecomnota\database\cafecomnota_1_0_16_conta_senha_google.sql

## Configuração necessária no Supabase

No Supabase Dashboard:

Authentication -> URL Configuration -> Redirect URLs

Adicionar:

cafecomnota://auth/callback
cafecomnota://auth/reset-password

Depois:

Authentication -> Providers -> Google

Ativar Google e informar Client ID e Client Secret do Google Cloud.

No Google Cloud OAuth, a callback URL do Supabase normalmente é:

https://unpdheecgblhgqxxqtop.supabase.co/auth/v1/callback

## Build e instalação

cd D:\projetos\Cafecomnota

.\gradlew.bat assembleDebug

adb install -r app\build\outputs\apk\debug\app-debug.apk

adb shell am start -n com.maurofmorato.cafecomnota/.MainActivity

## Testes

### Trocar senha logado

1. Perfil.
2. Entrar com e-mail/senha.
3. Informar nova senha e confirmação.
4. Salvar nova senha.
5. Sair.
6. Entrar com a nova senha.

### Esqueci minha senha

1. Perfil sem login.
2. Digitar e-mail.
3. Tocar em Esqueci minha senha.
4. Abrir e-mail no celular.
5. Clicar no link.
6. O app deve abrir e mostrar a conta conectada.
7. Informar nova senha e salvar.

### Google

1. Perfil sem login.
2. Tocar em Entrar com Google.
3. Concluir no navegador.
4. O app deve voltar pelo deep link e mostrar a conta conectada.

Se o Google não voltar para o app, revise as Redirect URLs no Supabase.
Se o Google nem iniciar, revise o Provider Google no Supabase e as credenciais no Google Cloud.

## Checkpoint Git

cd D:\projetos\Cafecomnota

git status
git add .
git commit -m "versao 1.0.16 - conta senha e login google"
git push

echo "git concluído"

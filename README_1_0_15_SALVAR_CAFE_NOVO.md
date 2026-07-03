# Café com nota - Versão 1.0.15

## Objetivo

Permitir salvar café novo pelo aplicativo, gravando em `public.cafes` no Supabase.

## O que mudou

1. A tela **Cadastrar café** agora salva no Supabase.
2. Usuário precisa estar logado para cadastrar.
3. Nome e marca preservam acentuação.
4. O app salva:
   - nome
   - marca
   - tipo_cafe
   - torra
   - peso_padrao_g
   - origem_dado = usuario
   - fonte_dado = Usuário
   - cadastrado_por = usuário logado
5. Se o usuário for admin, o café entra como `ativo`.
6. Se o usuário não for admin, o café entra como `pendente`.
7. A Home mostra `teste 1.0.15`.
8. versionCode = 9.
9. versionName = 1.0.15.

## Arquivos alterados/criados

- app/build.gradle.kts
- app/src/main/java/com/maurofmorato/cafecomnota/CafeComNotaApp.kt
- app/src/main/java/com/maurofmorato/cafecomnota/data/coffee/CoffeeCreateRequest.kt
- app/src/main/java/com/maurofmorato/cafecomnota/data/coffee/SupabaseCoffeeWriteRepository.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/AddCoffeeScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/HomeScreen.kt
- database/cafecomnota_1_0_15_salvar_cafe_novo.sql

## Como aplicar

Salve este ZIP em:

D:\temp\cafecomnota\

Execute:

cd D:\temp\cafecomnota

Expand-Archive -Path .\cafecomnota_1_0_15_salvar_cafe_novo_20260703.zip -DestinationPath D:\projetos\Cafecomnota -Force

## SQL

Execute no Supabase SQL Editor:

D:\projetos\Cafecomnota\database\cafecomnota_1_0_15_salvar_cafe_novo.sql

## Build e instalação

cd D:\projetos\Cafecomnota

.\gradlew.bat assembleDebug

adb install -r app\build\outputs\apk\debug\app-debug.apk

adb shell am start -n com.maurofmorato.cafecomnota/.MainActivity

## Teste recomendado

1. Entrar com mauro.fonseca@gmail.com, que é admin.
2. Ir em Cadastrar café novo.
3. Cadastrar:
   - Nome: Café Teste São João
   - Marca: Torrefação do Mauro
   - Peso: 250
   - Tipo: Moído ou Grãos
   - Torra: Média
4. Salvar.
5. Como admin, o café deve entrar ativo.
6. Voltar para Home/Buscar e procurar pelo nome.
7. Conferir no Supabase:
   select nome, marca, tipo_cafe, torra, status, cadastrado_por
   from public.cafes
   where nome ilike '%São João%';

## Checkpoint Git

cd D:\projetos\Cafecomnota

git status
git add .
git commit -m "versao 1.0.15 - salva cafe novo no supabase"
git push

echo "git concluído"

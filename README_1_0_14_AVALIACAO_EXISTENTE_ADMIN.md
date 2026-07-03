# Café com nota - versão 1.0.14

## Objetivo

Esta versão junta duas melhorias importantes para acelerar o envio para testes/publicação:

1. Ao abrir "Dar minha nota", o app busca a última avaliação/preço daquele usuário para aquele café.
2. Cria a primeira base administrativa para moderação pelo celular.

## O que mudou no app

### Avaliação existente

Quando o usuário já avaliou um café, ao abrir a tela "Dar minha nota" o app tenta carregar:

- nota
- compraria novamente
- preço atual do usuário
- peso
- método de preparo
- comentário

A tela passa a funcionar como edição da própria avaliação.

### Administração inicial

- O app consulta a função RPC `usuario_atual_is_admin()`.
- Se o usuário logado for admin, o Perfil mostra o bloco "Administração ativa".
- No detalhe de um café, o admin passa a ver ações:
  - Ocultar café
  - Marcar como pendente
  - Reativar café

A moderação usa RLS no Supabase. O botão só aparecer no app não é a segurança principal; a segurança real fica no banco.

### Versão

- versionCode: 8
- versionName: 1.0.14
- Home mostra "teste 1.0.14"

## Arquivos alterados/criados

- app/build.gradle.kts
- app/src/main/java/com/maurofmorato/cafecomnota/CafeComNotaApp.kt
- app/src/main/java/com/maurofmorato/cafecomnota/data/admin/SupabaseAdminRepository.kt
- app/src/main/java/com/maurofmorato/cafecomnota/data/review/ExistingReviewData.kt
- app/src/main/java/com/maurofmorato/cafecomnota/data/review/SupabaseReviewRepository.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/CoffeeDetailScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/HomeScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/ProfileScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/ReviewCoffeeScreen.kt
- database/cafecomnota_1_0_14_avaliacao_existente_admin.sql

## Como aplicar

Extraia o ZIP por cima do projeto:

D:\projetos\Cafecomnota

Depois rode o SQL no Supabase:

database\cafecomnota_1_0_14_avaliacao_existente_admin.sql

## Ativar seu usuário como administrador

Após executar o SQL, ele mostra uma lista de usuários.

Rode manualmente:

```sql
update public.usuarios
set is_admin = true
where id = 'COLE_AQUI_O_ID_DO_USUARIO_ADMIN';
```

Depois saia e entre novamente no app, ou feche e abra o app.

## Teste no celular

```powershell
cd D:\projetos\Cafecomnota

.\gradlew.bat assembleDebug

adb install -r app\build\outputs\apk\debug\app-debug.apk

adb shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

## O que testar

1. Entre no Perfil.
2. Faça login.
3. Abra um café que você já avaliou.
4. Toque em "Dar minha nota".
5. A tela deve carregar sua avaliação anterior.
6. Altere a nota/preço/comentário.
7. Salve.
8. Abra novamente "Dar minha nota".
9. Deve carregar os dados atualizados.

Para admin:

1. Execute o SQL.
2. Ative `is_admin = true` para seu usuário.
3. Saia e entre novamente no app.
4. Perfil deve mostrar "Administração ativa".
5. Abra o detalhe de um café.
6. Deve aparecer o bloco "Administração".
7. Teste "Marcar como pendente" em um café de teste.

## Checkpoint Git

```powershell
cd D:\projetos\Cafecomnota

git status
git add .
git commit -m "versao 1.0.14 - carrega avaliacao existente e base admin"
git push

echo "git concluído"
```

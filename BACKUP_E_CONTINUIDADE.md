# Rotina de backup do Café com nota

Execute esta rotina **antes de aplicar qualquer ZIP ou iniciar uma nova versão**.

## 1. Conferir alterações locais

```powershell
cd D:\projetos\Cafecomnota
git status
```

Não descarte arquivos alterados sem conferir a origem.

## 2. Criar uma cópia segura dos fontes

O script abaixo já exclui senhas, chaves, builds e configurações locais:

```powershell
.\gerar_zip_fontes_cafecomnota.ps1
```

O ZIP será gravado em `D:\temp\cafecomnota` com data e hora no nome.

## 3. Registrar a versão no Git

Adicione somente arquivos conhecidos. Não use `git add .` quando houver arquivos locais ou sensíveis.

```powershell
git add app\build.gradle.kts
git add app\src\main
git add LEIA_ME_ATUALIZACAO_1_0_19.md
git add BACKUP_E_CONTINUIDADE.md

git status
git commit -m "Prepara Cafe com nota 1.0.19"
git push
```

## 4. Proteger a chave de publicação

Mantenha cópias separadas e protegidas destes arquivos:

- chave `.jks` ou `.keystore`;
- `keystore.properties`;
- senhas da chave;
- `google-services.json`;
- dados de recuperação das contas Google, GitHub e Supabase.

Esses arquivos **não entram no GitHub nem nos ZIPs compartilhados**. Guarde uma cópia em mídia externa ou cofre criptografado. Sem a chave de assinatura, futuras atualizações do mesmo aplicativo podem ficar bloqueadas.

# Café com nota — atualização 1.0.24

## O que muda

- Administração agora começa em uma lista simples de cafés.
- Toque em um café para abrir uma tela exclusiva de conferência e edição.
- Filtros de status passam a rolar na horizontal, sem quebrar ou empurrar os botões para fora da tela.
- O motivo de moderação vazio não aparece mais como `null`.
- Novo botão **Remover do aplicativo**, com confirmação.
- Cabeçalho de subtelas mais compacto e alinhado em telas estreitas.

## Como funciona a remoção

Remover tira o café da busca, do ranking e das telas públicas. O registro e o motivo ficam preservados para auditoria administrativa. Isso evita apagar dados e fotos de forma irreversível por engano.

## Antes de testar

No Supabase, abra **SQL Editor**, execute o arquivo abaixo inteiro e confirme o resultado:

`database/cafecomnota_1_0_24_administracao_lista_remocao.sql`

Depois, gere e instale o APK de depuração:

```powershell
cd D:\projetos\Cafecomnota
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    throw "A compilação falhou. Não instale o APK anterior."
}

adb -s emulator-5554 install -r .\app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 shell am force-stop com.maurofmorato.cafecomnota
adb -s emulator-5554 shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

## Roteiro rápido de verificação

1. Entre com o usuário administrador e abra **Perfil → Administração de cafés**.
2. Confirme que a lista mostra apenas cartões resumidos e que os filtros rolam horizontalmente.
3. Toque em um café, altere um dado ou status e salve.
4. Em outro café de teste, escolha **Remover do aplicativo** e confirme.
5. Verifique que o item some da busca/ranking e aparece no filtro **Removido** na administração.

## Publicação fechada

Após o teste, gere o pacote assinado:

```powershell
& .\gradlew.bat bundleRelease
```

Envie `app\build\outputs\bundle\release\app-release.aab` para a faixa de **teste fechado**, com as notas curtas:

`Administração de cafés simplificada, correções de layout e remoção administrativa com auditoria.`

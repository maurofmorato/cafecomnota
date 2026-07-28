# Café com nota — atualização 1.0.25

Esta atualização corrige o fluxo de remoção na administração.

## Alterações

- versão `1.0.25` (`versionCode 20`);
- cafés removidos deixam de aparecer no filtro **Todos**;
- após uma remoção confirmada no banco, o item é ocultado imediatamente ao voltar para a lista;
- o filtro **Removido** continua disponível apenas para consulta de auditoria;
- filtros de status passam a ocupar mais de uma linha quando necessário, sem cortar palavras;
- aviso de rolagem fica menor e afastado dos controles;
- SQL de status corrigido para tratar os nomes antigos `cafes_status_chk` e `cafes_status_check`.

## Banco de dados

Você já aplicou a correção manualmente. Para referência futura, o arquivo abaixo é seguro e idempotente:

`database/cafecomnota_1_0_25_correcao_status_removido.sql`

## Compilar e testar no emulador

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

## Teste recomendado

1. Entre com a conta administradora.
2. Abra **Administração de cafés**.
3. Remova um café de teste.
4. Ao retornar, confirme que ele não aparece no filtro **Todos**.
5. Abra o filtro **Removido** para confirmar que o registro continua acessível para auditoria.

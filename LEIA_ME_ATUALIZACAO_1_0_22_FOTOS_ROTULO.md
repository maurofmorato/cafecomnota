# Café com nota — versão 1.0.22 (build 17)

## O que muda

- Cadastro aceita até **cinco fotos** do pacote.
- Cada foto pode ser identificada como frente, verso, lateral, informações, código de barras ou outra.
- É possível refazer ou remover uma foto antes de salvar.
- As leituras de texto das fotos são combinadas para sugerir a ficha técnica.
- A primeira foto enviada será a capa quando o café for aprovado.
- As imagens são reduzidas para no máximo 1600 px no maior lado e convertidas para JPEG antes do envio.

## Primeiro teste: emulador

O emulador é suficiente para conferir a estabilidade da nova tela, os chips de tipo de foto,
o limite de cinco itens, a rolagem horizontal e os botões de remover/refazer.

Não é necessário executar SQL para esse teste visual, desde que não seja salvo um café com fotos.

## Antes de testar fotos reais ou publicar

No painel do Supabase, abra **SQL Editor** e execute uma única vez, por inteiro:

`database/cafecomnota_1_0_22_fotos_rotulo.sql`

Esse script cria:

- o bucket privado `cafe-rotulos`;
- a tabela `public.cafe_fotos`;
- limite de cinco fotos por café;
- regras para que fotos pendentes sejam vistas pelo autor e pelo administrador;
- liberação pública apenas depois que o café estiver com status `ativo`.

Não execute o script pela metade e não altere as políticas nele sem conferência.

## Compilação e instalação no emulador

```powershell
cd D:\projetos\Cafecomnota

& .\gradlew.bat assembleDebug
$BuildOk = ($LASTEXITCODE -eq 0)

if (-not $BuildOk) {
    throw "A compilação falhou. Não instale o APK anterior."
}

adb devices
adb -s emulator-5554 install -r .\app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 shell am force-stop com.maurofmorato.cafecomnota
adb -s emulator-5554 shell am start -n com.maurofmorato.cafecomnota/.MainActivity
```

## Conferência esperada

Na tela **Cadastrar café**, a seção **Ler rótulo pela câmera** deve mostrar:

1. chips para escolher o tipo da próxima foto;
2. o cartão `Adicionar Frente` inicialmente;
3. contador `0/5 fotos`;
4. após cada foto, cartão com `Refazer` e botão `X` para removê-la.

## Publicação futura

Antes de enviar a versão à faixa de teste fechado, teste pelo menos um cadastro com foto
em um celular físico e confirme no Supabase que há uma linha em `public.cafe_fotos`.

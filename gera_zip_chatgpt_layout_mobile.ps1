# ============================================================
# Café com nota - gerar ZIP de fontes para análise no ChatGPT
#
# Script fica em:
# D:\projetos\Cafecomnota\gera_zip_chatgpt_layout_mobile.ps1
#
# ZIP gerado em:
# D:\temp\cafecomnota\cafecomnota_fontes_layout_mobile_20260703.zip
# ============================================================

$Projeto = "D:\projetos\Cafecomnota"
$DestinoZip = "D:\temp\cafecomnota"
$NomeZip = "cafecomnota_fontes_layout_mobile_20260703.zip"
$Zip = Join-Path $DestinoZip $NomeZip

$Temporario = Join-Path $DestinoZip "_zip_chatgpt_layout_mobile"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " Café com nota - Gerando ZIP para o ChatGPT" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

if (!(Test-Path $Projeto)) {
    Write-Host "[ERRO] Projeto não encontrado: $Projeto" -ForegroundColor Red
    exit 1
}

if (!(Test-Path $DestinoZip)) {
    New-Item -ItemType Directory -Path $DestinoZip -Force | Out-Null
}

if (Test-Path $Zip) {
    Remove-Item $Zip -Force
}

if (Test-Path $Temporario) {
    Remove-Item $Temporario -Recurse -Force
}

New-Item -ItemType Directory -Path $Temporario -Force | Out-Null

$Arquivos = @(
    "app\src\main\java\com\maurofmorato\cafecomnota\CafeComNotaApp.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\MainActivity.kt",

    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\HomeScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\RankingScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\ReviewCoffeeScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\CoffeeDetailScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\SearchScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\AddCoffeeScreen.kt",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\screens\ProfileScreen.kt",

    "app\src\main\java\com\maurofmorato\cafecomnota\ui\components",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\theme",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\model",
    "app\src\main\java\com\maurofmorato\cafecomnota\ui\i18n",

    "app\src\main\java\com\maurofmorato\cafecomnota\data\repository",
    "app\src\main\java\com\maurofmorato\cafecomnota\data\review",
    "app\src\main\java\com\maurofmorato\cafecomnota\data\supabase",
    "app\src\main\java\com\maurofmorato\cafecomnota\data\auth",

    "app\build.gradle.kts",
    "build.gradle.kts",
    "settings.gradle.kts"
)

foreach ($Relativo in $Arquivos) {
    $Origem = Join-Path $Projeto $Relativo

    if (Test-Path $Origem) {
        $DestinoArquivo = Join-Path $Temporario $Relativo
        $PastaDestino = Split-Path $DestinoArquivo -Parent

        if (!(Test-Path $PastaDestino)) {
            New-Item -ItemType Directory -Path $PastaDestino -Force | Out-Null
        }

        Copy-Item $Origem $DestinoArquivo -Recurse -Force
        Write-Host "[OK] $Relativo" -ForegroundColor Green
    }
    else {
        Write-Host "[AVISO] Não encontrado: $Relativo" -ForegroundColor Yellow
    }
}

Compress-Archive -Path "$Temporario\*" -DestinationPath $Zip -Force

Remove-Item $Temporario -Recurse -Force

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host " ZIP gerado com sucesso" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host $Zip -ForegroundColor Cyan
Write-Host ""
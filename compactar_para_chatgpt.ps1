# ============================================================
# Café com nota
# Script para compactar arquivos importantes do projeto
# para envio ao ChatGPT
# ============================================================

$Projeto = "D:\projetos\Cafecomnota"
$Destino = "D:\temp\cafecomnota"

$DataHora = Get-Date -Format "yyyyMMdd_HHmmss"
$NomeZip = "cafecomnota_envio_$DataHora.zip"
$CaminhoZip = Join-Path $Destino $NomeZip

Write-Host ""
Write-Host "============================================================"
Write-Host " Café com nota - Compactar projeto para envio"
Write-Host "============================================================"
Write-Host ""

if (!(Test-Path $Projeto)) {
    Write-Host "ERRO: Pasta do projeto não encontrada:"
    Write-Host $Projeto
    exit 1
}

if (!(Test-Path $Destino)) {
    New-Item -ItemType Directory -Path $Destino | Out-Null
}

if (Test-Path $CaminhoZip) {
    Remove-Item $CaminhoZip -Force
}

$Arquivos = @(
    "settings.gradle.kts",
    "build.gradle.kts",
    "gradle.properties",
    "gradle\libs.versions.toml",
    "app\build.gradle.kts",
    "app\src\main\AndroidManifest.xml",
    "app\src\main\java\com\maurofmorato\cafecomnota\MainActivity.kt"
)

$Temp = Join-Path $Destino "cafecomnota_temp_envio"

if (Test-Path $Temp) {
    Remove-Item $Temp -Recurse -Force
}

New-Item -ItemType Directory -Path $Temp | Out-Null

foreach ($Arquivo in $Arquivos) {
    $Origem = Join-Path $Projeto $Arquivo
    $Alvo = Join-Path $Temp $Arquivo
    $PastaAlvo = Split-Path $Alvo -Parent

    if (!(Test-Path $PastaAlvo)) {
        New-Item -ItemType Directory -Path $PastaAlvo -Force | Out-Null
    }

    if (Test-Path $Origem) {
        Copy-Item $Origem $Alvo -Force
        Write-Host "OK  - $Arquivo"
    } else {
        Write-Host "AVISO - Não encontrado: $Arquivo"
    }
}

Compress-Archive -Path (Join-Path $Temp "*") -DestinationPath $CaminhoZip -Force

Remove-Item $Temp -Recurse -Force

Write-Host ""
Write-Host "ZIP gerado com sucesso:"
Write-Host $CaminhoZip
Write-Host ""
Write-Host "Envie esse arquivo aqui no chat."
Write-Host ""
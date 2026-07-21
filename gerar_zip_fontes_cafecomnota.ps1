# Gera um ZIP dos fontes do Café com nota para análise/continuidade.
# O projeto fica em D:\projetos\Cafecomnota e o ZIP sai em D:\temp\cafecomnota.
# Não inclui senhas, chaves, builds, arquivos locais do Android Studio ou Git.

$ErrorActionPreference = 'Stop'

$Projeto = 'D:\projetos\Cafecomnota'
$Destino = 'D:\temp\cafecomnota'
$NomeZip = 'cafecomnota_fontes_{0}.zip' -f (Get-Date -Format 'yyyyMMdd_HHmmss')
$Zip = Join-Path $Destino $NomeZip
$PastaTemporaria = Join-Path $env:TEMP ('cafecomnota_fontes_' + [Guid]::NewGuid().ToString('N'))

if (-not (Test-Path -LiteralPath $Projeto)) {
    throw "Projeto não encontrado em: $Projeto"
}

New-Item -ItemType Directory -Path $Destino -Force | Out-Null
New-Item -ItemType Directory -Path $PastaTemporaria -Force | Out-Null

# Pastas geradas/localmente configuradas que nunca devem ir ao ZIP.
$PastasIgnoradas = @(
    '.git', '.gradle', '.idea', '.kotlin',
    'build', 'out', 'node_modules', '.externalNativeBuild'
)

# Arquivos que podem guardar dados locais, senhas ou chaves.
$ArquivosIgnorados = @(
    'local.properties',
    'keystore.properties',
    'google-services.json',
    '.env', '.env.local', '.env.production', '.env.development'
)

$ExtensoesIgnoradas = @(
    '.jks', '.keystore', '.p12', '.pfx', '.pem', '.key',
    '.apk', '.aab', '.apks', '.zip', '.log'
)

try {
    $Arquivos = Get-ChildItem -LiteralPath $Projeto -Recurse -File | Where-Object {
        $CaminhoRelativo = $_.FullName.Substring($Projeto.Length).TrimStart('\', '/')
        $Partes = $CaminhoRelativo -split '[\\/]'
        $PossuiPastaIgnorada = $Partes | Where-Object { $PastasIgnoradas -contains $_ }
        $NomeIgnorado = $ArquivosIgnorados -contains $_.Name
        $ExtensaoIgnorada = $ExtensoesIgnoradas -contains $_.Extension.ToLowerInvariant()
        $NomePareceSecreto = $_.Name -match '(?i)(senha|password|secret|credential|token).*\.(properties|json|xml|txt|env)$'

        -not $PossuiPastaIgnorada -and -not $NomeIgnorado -and -not $ExtensaoIgnorada -and -not $NomePareceSecreto
    }

    foreach ($Arquivo in $Arquivos) {
        $CaminhoRelativo = $Arquivo.FullName.Substring($Projeto.Length).TrimStart('\', '/')
        $DestinoArquivo = Join-Path $PastaTemporaria $CaminhoRelativo
        $PastaArquivo = Split-Path -Parent $DestinoArquivo

        New-Item -ItemType Directory -Path $PastaArquivo -Force | Out-Null
        Copy-Item -LiteralPath $Arquivo.FullName -Destination $DestinoArquivo -Force
    }

    if (-not (Get-ChildItem -LiteralPath $PastaTemporaria -Recurse -File)) {
        throw 'Nenhum arquivo foi selecionado para o ZIP.'
    }

    if (Test-Path -LiteralPath $Zip) {
        Remove-Item -LiteralPath $Zip -Force
    }

    Compress-Archive -Path (Join-Path $PastaTemporaria '*') -DestinationPath $Zip -Force

    Write-Host ''
    Write-Host 'ZIP criado com sucesso:' -ForegroundColor Green
    Write-Host $Zip -ForegroundColor Cyan
    Write-Host ''
    Write-Host 'Arquivos sensíveis e artefatos locais foram excluídos.' -ForegroundColor Yellow
}
finally {
    if (Test-Path -LiteralPath $PastaTemporaria) {
        Remove-Item -LiteralPath $PastaTemporaria -Recurse -Force
    }
}

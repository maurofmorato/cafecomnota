[CmdletBinding()]
param(
    [string]$ProjectRoot = $PSScriptRoot,
    [string]$OutputDirectory = 'D:\temp\cafecomnota'
)

$ErrorActionPreference = 'Stop'

function Test-ExcludedPath {
    param([Parameter(Mandatory)][string]$RelativePath)

    $normalized = $RelativePath.Replace('\', '/')
    $fileName = [System.IO.Path]::GetFileName($normalized)

    if ($normalized -match '(^|/)(\.git|\.gradle|build|\.idea|entrega_.*)(/|$)') { return $true }
    if ($fileName -match '(?i)^cafecomnota_1_0_.*\.zip$') { return $true }
    if ($fileName -eq 'gerar_zip_cafecomnota_para_analise.ps1') { return $true }
    if ($fileName -in @('local.properties', 'keystore.properties', 'keystore.properties.example')) { return $true }
    if ($fileName -match '\.(jks|keystore|p12|pfx|pem|key)$') { return $true }
    if ($fileName -match '^\.env(\..+)?$') { return $true }
    if ($fileName -eq 'google-services.json') { return $true }
    if ($fileName -match '(?i)(client[_-]?secret|oauth|credential|credentials|service[_-]?account|secret).*\.(json|pem|key|p12|pfx)$') { return $true }

    return $false
}

function Redact-SensitiveLine {
    param(
        [Parameter(Mandatory)][AllowEmptyString()][string]$Line,
        [Parameter(Mandatory)][string]$RelativePath
    )

    $isConfigurationFile = $RelativePath -match '(?i)(SupabaseConfig|BuildConfig|secrets|credentials|config)\.(kt|java|properties|json)$'
    if (-not $isConfigurationFile) { return $Line }

    $sensitiveName = '(?i)(anon[_-]?key|publishable[_-]?key|service[_-]?role|api[_-]?key|access[_-]?token|refresh[_-]?token|secret|password|supabase[_-]?key)'
    if ($Line -notmatch $sensitiveName) { return $Line }

    if ($Line -match '^(\s*[^=:\s]+\s*[=:]\s*)') { return $Matches[1] + '"REDACTED"' }
    if ($Line -match '^(\s*(?:const\s+)?val\s+[^=]+?\s*=\s*)') { return $Matches[1] + '"REDACTED"' }

    return '// REDACTED: sensitive configuration line removed from analysis package'
}

$resolvedProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
if (-not (Test-Path -LiteralPath (Join-Path $resolvedProjectRoot '.git'))) {
    throw "Git repository not found at '$resolvedProjectRoot'."
}

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$stageDirectory = Join-Path $env:TEMP "cafecomnota_analise_seguro_$timestamp"
$zipPath = Join-Path $OutputDirectory "cafecomnota_para_analise_seguro_$timestamp.zip"
New-Item -ItemType Directory -Path $stageDirectory -Force | Out-Null

try {
    $gitFiles = & git -C $resolvedProjectRoot ls-files --cached --others --exclude-standard
    if ($LASTEXITCODE -ne 0) {
        throw 'git ls-files failed.'
    }

    $excludedSensitiveFiles = @($gitFiles | Where-Object {
        $_ -match '(?i)(client[_-]?secret|oauth|credential|credentials|service[_-]?account|secret).*\.(json|pem|key|p12|pfx)$'
    })

    $includedFiles = 0
    foreach ($relativePath in $gitFiles) {
        if ([string]::IsNullOrWhiteSpace($relativePath)) { continue }
        if (Test-ExcludedPath -RelativePath $relativePath) { continue }

        $sourcePath = Join-Path $resolvedProjectRoot $relativePath
        if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) { continue }

        $destinationPath = Join-Path $stageDirectory $relativePath
        New-Item -ItemType Directory -Path (Split-Path -Parent $destinationPath) -Force | Out-Null

        $extension = [System.IO.Path]::GetExtension($sourcePath).ToLowerInvariant()
        if ($extension -in @('.kt', '.java', '.kts', '.gradle', '.properties', '.json', '.xml', '.sql', '.md', '.txt', '.yml', '.yaml')) {
            $content = Get-Content -LiteralPath $sourcePath -Encoding UTF8
            $redactedContent = foreach ($line in $content) {
                Redact-SensitiveLine -Line $line -RelativePath $relativePath
            }
            Set-Content -LiteralPath $destinationPath -Value $redactedContent -Encoding UTF8
        }
        else {
            Copy-Item -LiteralPath $sourcePath -Destination $destinationPath -Force
        }

        $includedFiles++
    }

    @(
        'Safe analysis package for Cafe com nota'
        "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        "Source: $resolvedProjectRoot"
        "Included files: $includedFiles"
        'Excluded: Git data, generated files, IDE data, temporary deliveries, local signing files, environment files and OAuth credentials.'
        'Sensitive configuration values are redacted.'
    ) | Set-Content -LiteralPath (Join-Path $stageDirectory 'MANIFESTO_DO_PACOTE.txt') -Encoding UTF8

    Compress-Archive -Path (Join-Path $stageDirectory '*') -DestinationPath $zipPath -CompressionLevel Optimal

    $zip = Get-Item -LiteralPath $zipPath
    Write-Host ''
    Write-Host 'Safe ZIP generated successfully:' -ForegroundColor Green
    Write-Host $zip.FullName
    Write-Host ("Size: {0:N2} MB" -f ($zip.Length / 1MB))
    Write-Host ("Included files: {0}" -f $includedFiles)

    if ($excludedSensitiveFiles.Count -gt 0) {
        Write-Warning 'Sensitive files were found and excluded from the ZIP:'
        $excludedSensitiveFiles | ForEach-Object { Write-Warning " - $_" }
    }
}
finally {
    if (Test-Path -LiteralPath $stageDirectory) {
        Remove-Item -LiteralPath $stageDirectory -Recurse -Force
    }
}

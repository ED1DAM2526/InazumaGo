param(
    [switch]$SkipTests,
    [string]$ReleaseDir = $env:INAZUMAGO_RELEASE_DIR
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

if (-not $ReleaseDir -or [string]::IsNullOrWhiteSpace($ReleaseDir)) {
    $ReleaseDir = Join-Path $projectRoot 'target\releases'
}

# Manejo simple de variable JAVA_HOME: usa entorno actual o lee doc/ia/user-prompt.md.
$userPromptPath = Join-Path $projectRoot 'doc\ia\user-prompt.md'
if (-not $env:JAVA_HOME -and (Test-Path $userPromptPath)) {
    $userPromptContent = Get-Content $userPromptPath -Raw
    if ($userPromptContent -match '(?m)^\s*JDK_PATH\s*=\s*(.+?)\s*$') {
        $candidate = $matches[1].Trim('"','''',' ')
        if (Test-Path (Join-Path $candidate 'bin\java.exe')) {
            $env:JAVA_HOME = $candidate
        }
    }
    elseif ($userPromptContent -match "\$env:JAVA_HOME\s*=\s*'([^']+)'") {
        $candidate = $matches[1]
        if (Test-Path (Join-Path $candidate 'bin\java.exe')) {
            $env:JAVA_HOME = $candidate
        }
    }
}

if ($env:JAVA_HOME) {
    $javaBin = Join-Path $env:JAVA_HOME 'bin'
    if (-not (($env:Path -split ';') -contains $javaBin)) {
        $env:Path = "$javaBin;$env:Path"
    }
}

$mvnw = Join-Path $projectRoot 'mvnw.cmd'
if (-not (Test-Path $mvnw)) {
    Write-Error "No se encontro mvnw.cmd en la raiz del proyecto."
    exit 1
}

Push-Location $projectRoot
try {
    $mavenArgs = @('clean', 'package')
    if ($SkipTests -or $env:INAZUMAGO_PACKAGE_SKIP_TESTS -eq 'true') {
        $mavenArgs += '-DskipTests=true'
    }

    Write-Host "Ejecutando Maven Wrapper: $($mavenArgs -join ' ')" -ForegroundColor Cyan
    & $mvnw @mavenArgs
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    $jar = Get-ChildItem -Path (Join-Path $projectRoot 'target') -Filter '*.jar' |
        Where-Object {
            $_.Name -notlike '*-sources.jar' -and
            $_.Name -notlike '*-javadoc.jar' -and
            $_.Name -notlike 'original-*.jar'
        } |
        Select-Object -First 1

    if (-not $jar) {
        Write-Error 'No se encontro un JAR empaquetado en target/.'
        exit 2
    }

    New-Item -ItemType Directory -Path $ReleaseDir -Force | Out-Null
    $destination = Join-Path $ReleaseDir $jar.Name
    Copy-Item -Path $jar.FullName -Destination $destination -Force

    Write-Host "Packaging completado. Artefacto: $destination" -ForegroundColor Green
}
finally {
    Pop-Location
}

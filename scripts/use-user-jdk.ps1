param(
    [string]$JdkPath,
    [switch]$NonInteractive
)

# Este script aplica la ruta al JDK indicada por el usuario a la sesión actual.
# NOTA: Versión estricta: no hace preguntas interactivas; si no encuentra JDK_PATH o java.exe
# aborta con un código de salida y un mensaje. Uso recomendado (desde la raíz del proyecto):
#   powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -NonInteractive
#   powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath 'C:\ruta\a\jdk-21'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$defaultUserPrompt = Join-Path $scriptDir "..\doc\ia\user-prompt.md"

function Write-Log($m) { Write-Host $m }

# Si no se proporcionó como parámetro, intentar leer JDK_PATH desde doc/ia/user-prompt.md
if (-not $JdkPath) {
    if (Test-Path $defaultUserPrompt) {
        try {
            $content = Get-Content $defaultUserPrompt -Raw -ErrorAction Stop
            if ($content -match '(?m)^\s*JDK_PATH\s*=\s*(.+)\s*$') {
                $raw = $matches[1].Trim()
                # eliminar comillas simples o dobles alrededor si existen
                $JdkPath = $raw.Trim([char]39, [char]96, [char]34, [char]32)
                Write-Log ("JDK_PATH leída de ${defaultUserPrompt}: $JdkPath")
            }
        } catch {
            Write-Host ("No se pudo leer ${defaultUserPrompt}: $_") -ForegroundColor Yellow
        }
    }
}

# Modo estricto: si no hay JDK_PATH válido, abortar inmediatamente (sin modo interactivo)
if (-not $JdkPath) {
    Write-Host 'ERROR: `JDK_PATH` no encontrado en `doc/ia/user-prompt.md`. Configure la ruta al JDK 21 en ese archivo y vuelva a intentarlo.' -ForegroundColor Red
    exit 2
}

# Validar existencia mínima: bin\java.exe
$javaExe = Join-Path $JdkPath 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    Write-Host "ERROR: java.exe no encontrado en: $javaExe. Comprueba la ruta JDK_PATH y vuelve a intentarlo." -ForegroundColor Red
    exit 3
}

# Aplicar a la sesión actual (no persistente)
$env:JAVA_HOME = $JdkPath
$binPath = Join-Path $env:JAVA_HOME 'bin'
$pathParts = $env:Path -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
if (-not ($pathParts -contains $binPath)) {
    $env:Path = "$binPath;$env:Path"
    Write-Host "Se añadió $binPath al PATH de la sesión."
} else {
    Write-Host "$binPath ya está presente en PATH."
}

Write-Host "JAVA_HOME establecido en: $env:JAVA_HOME"

# Mostrar java -version para trazabilidad
try {
    & "$javaExe" -version 2>&1 | ForEach-Object { Write-Host $_ }
} catch {
    Write-Warning "No se pudo ejecutar '$javaExe -version': $_"
}

Write-Host "Script use-user-jdk.ps1 finalizado. (Variables aplicadas solo en esta sesión)"
exit 0

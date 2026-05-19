# ============================================================================
# run-tests.ps1
# Script para ejecutar tests del proyecto InazumaGo con Maven
# ============================================================================
# Uso:
#   .\scripts\run-tests.ps1
#   .\scripts\run-tests.ps1 -Verbose
#
# Descripcion:
#   Ejecuta los tests del proyecto usando Maven (mvn -DskipTests=false test).
#   Los resultados se exportan a: target/surefire-reports/
#
# Variables de entorno esperadas:
#   - JAVA_HOME: ruta del JDK (si no esta configurada, Maven intenta usar la del PATH)
#   - Si quieres usar el JDK de doc/ia/user-prompt.md, ejecuta primero:
#     .\scripts\use-user-jdk.ps1
# ============================================================================

# ============================================================================
# run-tests.ps1
# Script simple para ejecutar tests del proyecto InazumaGo con Maven
# ============================================================================
# Uso:
#   .\scripts\run-tests.ps1
#   .\scripts\run-tests.ps1 -Verbose
# ============================================================================

param(
    [switch]$Verbose = $false
)

# Configuración
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Ejecutando tests de InazumaGo (usando Maven Wrapper)" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Comprobar disponibilidad de compilador (javac). Intentar auto-configuración desde scripts/check-jdk.ps1
$checkJdk = Join-Path $scriptDir 'check-jdk.ps1'
if (Test-Path $checkJdk) {
    Write-Host "[*] Comprobando JDK local (javac)..." -ForegroundColor Yellow
    powershell -ExecutionPolicy Bypass -File $checkJdk
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[*] Javac disponible. Continuando..." -ForegroundColor Green
    } else {
        Write-Host "[!] No se detectó javac automáticamente. Intentando obtener ruta desde doc/ia/user-prompt.md..." -ForegroundColor Yellow

        $userPrompt = Join-Path $projectRoot 'doc\ia\user-prompt.md'
        if (Test-Path $userPrompt) {
            $content = Get-Content $userPrompt -Raw -ErrorAction SilentlyContinue
            if ($content -match "\$env:JAVA_HOME\s*=\s*'([^']+)'") {
                $found = $matches[1]
                Write-Host "[*] Se encontró JAVA_HOME en $userPrompt: $found" -ForegroundColor Yellow
                $useJdkScript = Join-Path $scriptDir 'use-user-jdk.ps1'
                if (Test-Path $useJdkScript) {
                    Write-Host "[*] Aplicando JDK desde $userPrompt (no interactivo)..." -ForegroundColor Yellow
                    powershell -ExecutionPolicy Bypass -File $useJdkScript -JdkPath $found -NonInteractive
                    if ($LASTEXITCODE -ne 0) {
                        Write-Host "ERROR: no se pudo aplicar JDK desde $userPrompt (codigo $LASTEXITCODE)." -ForegroundColor Red
                        Write-Host "Por favor ejecuta: .\scripts\use-user-jdk.ps1 y proporciona la ruta al JDK o instala un JDK en el sistema." -ForegroundColor Red
                        exit $LASTEXITCODE
                    }
                } else {
                    Write-Host "ERROR: no se encontró scripts/use-user-jdk.ps1. Ejecuta manualmente para configurar JAVA_HOME." -ForegroundColor Red
                    exit 5
                }
            } else {
                Write-Host "No se encontró una entrada de JAVA_HOME en $userPrompt." -ForegroundColor Yellow
                Write-Host "Ejecuta .\scripts\use-user-jdk.ps1 para configurar el JDK de forma interactiva o instala un JDK en el sistema." -ForegroundColor Yellow
                exit 6
            }
        } else {
            Write-Host "No existe $userPrompt. Ejecuta .\scripts\use-user-jdk.ps1 para configurar JAVA_HOME o instala un JDK localmente." -ForegroundColor Yellow
            exit 7
        }
    }
} else {
    Write-Host "WARNING: no se encontró scripts/check-jdk.ps1. Asegúrate de tener un JDK instalado y javac en PATH." -ForegroundColor Yellow
}

# Cambiar al directorio del proyecto
Push-Location $projectRoot

try {
    Write-Host "[*] Ejecutando tests con Maven Wrapper (mvnw.cmd)..." -ForegroundColor Yellow
    Write-Host ""

    $mvnw = Join-Path $projectRoot 'mvnw.cmd'
    if (-not (Test-Path $mvnw)) {
        Write-Host "ERROR: No se encontró mvnw.cmd en la raíz del proyecto. Asegúrate de que el Maven Wrapper está presente." -ForegroundColor Red
        exit 4
    }

    if ($Verbose) {
        & cmd /c "`"$mvnw`" -X -DskipTests=false test"
    }
    else {
        & cmd /c "`"$mvnw`" -DskipTests=false test"
    }

    $exitCode = $LASTEXITCODE

    Write-Host ""
    Write-Host "===============================================" -ForegroundColor Cyan

    if ($exitCode -eq 0) {
        Write-Host "Tests ejecutados exitosamente!" -ForegroundColor Green

        $reportsDir = Join-Path $projectRoot "target\surefire-reports"
        if (Test-Path $reportsDir) {
            Write-Host "Reportes en: $reportsDir" -ForegroundColor White
        }
    }
    else {
        Write-Host "Los tests fallaron (codigo: $exitCode)" -ForegroundColor Red
    }

    Write-Host "===============================================" -ForegroundColor Cyan

    exit $exitCode
}
finally {
    Pop-Location
}

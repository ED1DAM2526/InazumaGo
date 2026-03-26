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

# Intentar aplicar JDK desde doc/ia/user-prompt.md en modo no interactivo.
# El script use-user-jdk.ps1 fallará con código distinto de 0 si no hay JDK_PATH válida.
$useJdkScript = Join-Path $scriptDir 'use-user-jdk.ps1'
if (Test-Path $useJdkScript) {
    Write-Host "[*] Aplicando JDK desde doc/ia/user-prompt.md (si existe)..." -ForegroundColor Yellow
    powershell -ExecutionPolicy Bypass -File $useJdkScript -NonInteractive
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: no se pudo aplicar el JDK desde doc/ia/user-prompt.md (código $LASTEXITCODE). Este script requiere que JDK esté configurado en ese archivo. Abortando." -ForegroundColor Red
        # Si el script hijo ya mostró un mensaje de error específico, no lo sobrescribimos; aquí reiteramos el mensaje conforme a la política estricta.
        Write-Host 'ERROR: `JDK_PATH` no encontrado en `doc/ia/user-prompt.md`. Configure la ruta al JDK 21 en ese archivo y vuelva a intentarlo.' -ForegroundColor Red
        exit $LASTEXITCODE
    }
} else {
    Write-Host "ERROR: no se encontró scripts/use-user-jdk.ps1. Este script requiere que exista y aplique el JDK desde doc/ia/user-prompt.md. Abortando." -ForegroundColor Red
    exit 5
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

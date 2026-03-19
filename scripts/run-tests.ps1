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
$jdkPath = $null

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Ejecutando tests de InazumaGo" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Buscar JDK si JAVA_HOME no está configurado
if (-not $env:JAVA_HOME) {
    Write-Host "[*] Buscando JDK..." -ForegroundColor Yellow

    # Búsqueda 1: Directorios estándar de Microsoft/Oracle/OpenJDK
    $jdkPaths = @(
        "C:\Users\$env:USERNAME\.jdks",
        "C:\Program Files\Java",
        "C:\Program Files (x86)\Java",
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles(x86)\Java"
    )

    foreach ($basePath in $jdkPaths) {
        if (Test-Path $basePath) {
            $jdks = Get-ChildItem -Path $basePath -Directory -Filter "jdk*" -ErrorAction SilentlyContinue |
                    Sort-Object -Property Name -Descending
            if ($jdks) {
                $jdkPath = $jdks[0].FullName
                break
            }

            # También buscar OpenJDK con prefijo ms-
            $msJdks = Get-ChildItem -Path $basePath -Directory -Filter "ms-*" -ErrorAction SilentlyContinue |
                      Sort-Object -Property Name -Descending
            if ($msJdks) {
                $jdkPath = $msJdks[0].FullName
                break
            }
        }
    }

    if ($jdkPath -and (Test-Path "$jdkPath\bin\java.exe")) {
        Write-Host "JDK encontrado: $jdkPath" -ForegroundColor Green
        $env:JAVA_HOME = $jdkPath
        $env:Path = "$jdkPath\bin;$env:Path"
    }
    else {
        Write-Host "JDK no encontrado. Continúo sin configuración..." -ForegroundColor Yellow
    }
}

# Cambiar al directorio del proyecto
Push-Location $projectRoot

try {
    Write-Host "[*] Ejecutando tests..." -ForegroundColor Yellow
    Write-Host ""

    # Ejecutar tests con Maven Wrapper
    if ($Verbose) {
        & cmd /c "mvnw.cmd -X -DskipTests=false test"
    }
    else {
        & cmd /c "mvnw.cmd -DskipTests=false test"
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









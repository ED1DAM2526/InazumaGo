param(
    [string]$JdkPath,
    [switch]$UseUserJdk
)

Set-Location "$PSScriptRoot\.."

function Write-ErrExit($msg) {
    Write-Host $msg -ForegroundColor Red
    exit 1
}

# Optionally prepare JDK via provided helper
if ($UseUserJdk) {
    Write-Host "Applying user JDK (use-user-jdk.ps1)..."
    powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath $JdkPath
}

# Check mvn availability
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnCmd) {
    Write-Host "Maven (mvn) no está en PATH. Puedes:
 - instalar Maven y añadirlo al PATH
 - o ejecutar .\scripts\use-user-jdk.ps1 y luego ejecutar mvn manualmente.
Aborto." -ForegroundColor Yellow
    exit 2
}

# Ensure port 8080 likely free (simple check)
$portInUse = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue) -ne $null
if ($portInUse) {
    Write-Host "Advertencia: el puerto 8080 parece estar en uso. WireMock usa 8080 por defecto." -ForegroundColor Yellow
}

# Run focused integration tests
$testArg = 'es.iesquevedo.integration.GameEventIntegrationTest,es.iesquevedo.repository.firebase.GameEventRepositoryTest,es.iesquevedo.service.impl.GameEventServiceImplTest'
Write-Host "Ejecutando tests: $testArg"
$mvnArgs = "-DskipTests=false", "-Dtest=$testArg", "test"

# Run mvn and capture output
$mvnProcess = Start-Process -FilePath mvn -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru -RedirectStandardOutput .\target\integration-test-output.txt -RedirectStandardError .\target\integration-test-error.txt

if ($mvnProcess.ExitCode -eq 0) {
    Write-Host "Tests ejecutados correctamente. Revisa target\integration-test-output.txt para la salida completa." -ForegroundColor Green
    exit 0
} else {
    Write-Host "Error en tests (código de salida: $($mvnProcess.ExitCode)). Revisa los archivos target\integration-test-output.txt y target\integration-test-error.txt" -ForegroundColor Red
    exit $mvnProcess.ExitCode
}


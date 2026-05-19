param(
    [string]$JdkPath
)

Write-Host "Comprobando entorno Java y compilador (javac)..."

function Show-Help {
    $help = @'
Solución rápida:
 1) Instala un JDK (por ejemplo Temurin/Adoptium o Oracle JDK).
 2) Establece JAVA_HOME en la ruta del JDK y añade %JAVA_HOME%\bin al PATH.
    Ejemplo (PowerShell, temporal para la sesión):
      $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
      $env:Path = "$env:JAVA_HOME\bin;$env:Path"
 3) Repite: .\mvnw.cmd -DskipTests=false test
'@
    Write-Host $help
}

try {
    $javac = Get-Command javac -ErrorAction SilentlyContinue
} catch {
    $javac = $null
}

if ($javac) {
    Write-Host "javac detectado: $($javac.Source)"
    & javac -version 2>&1 | ForEach-Object { Write-Host $_ }
    Write-Host "Comprobación completada: hay compilador disponible. Puedes ejecutar: .\mvnw.cmd -DskipTests=false test"
    exit 0
}

Write-Warning "No se detectó 'javac' en PATH. Es probable que tengas solo un JRE o no tengas JDK instalado."

if ($JdkPath) {
    if (Test-Path (Join-Path $JdkPath 'bin\javac.exe')) {
        Write-Host "Se encontró javac en la ruta proporcionada: $JdkPath. Aplicando a la sesión..."
        $env:JAVA_HOME = $JdkPath
        $env:Path = "$(Join-Path $env:JAVA_HOME 'bin');$env:Path"
        & javac -version 2>&1 | ForEach-Object { Write-Host $_ }
        Write-Host "JAVA_HOME establecido temporalmente. Ejecuta: .\mvnw.cmd -DskipTests=false test"
        exit 0
    } else {
        Write-Warning "La ruta proporcionada no contiene bin\javac.exe: $JdkPath"
    }
}

# Intento de auto-detección en rutas comunes (Windows)
function Find-JdkInCommonPaths {
    $candidates = @(
        'C:\Program Files\Java',
        'C:\Program Files (x86)\Java',
        'C:\Program Files\AdoptOpenJDK',
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Microsoft\jdk'
    )

    foreach ($base in $candidates) {
        if (Test-Path $base) {
            Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                $possible = Join-Path $_.FullName 'bin\javac.exe'
                if (Test-Path $possible) { return $_.FullName }
            }
        }
    }
    return $null
}

$foundAuto = Find-JdkInCommonPaths
if ($foundAuto) {
    Write-Host "Se detectó posible JDK en: $foundAuto. Aplicando a la sesión..." -ForegroundColor Green
    $env:JAVA_HOME = $foundAuto
    $env:Path = "$(Join-Path $env:JAVA_HOME 'bin');$env:Path"
    try { & javac -version 2>&1 | ForEach-Object { Write-Host $_ } } catch { }
    Write-Host "JAVA_HOME establecido temporalmente. Ejecuta: .\mvnw.cmd -DskipTests=false test"
    exit 0
}

Write-Host "Opciones:\n 1) Ejecutar scripts/use-user-jdk.ps1 para establecer JAVA_HOME localmente (interactivo).\n 2) Instalar un JDK y configurar JAVA_HOME.\n 3) Para CI, asegúrate de que la imagen tenga JDK (el pipeline de ejemplo usa eclipse-temurin:21).\n"
Show-Help
exit 1


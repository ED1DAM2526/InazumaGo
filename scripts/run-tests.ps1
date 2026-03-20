param(
    [string]$JdkPath,
    [string]$MavenCmdPath
)

$ErrorActionPreference = "Stop"

function Get-JdkFromUserPrompt {
    param([string]$UserPromptPath)

    if (-not (Test-Path $UserPromptPath)) {
        return $null
    }

    $content = Get-Content $UserPromptPath -Raw -ErrorAction SilentlyContinue
    if ($content -match "\$env:JAVA_HOME\s*=\s*'([^']+)'") {
        return $matches[1]
    }

    if ($content -match '\$env:JAVA_HOME\s*=\s*"([^"]+)"') {
        return $matches[1]
    }

    return $null
}

function Test-JdkPath {
    param([string]$Path)

    if (-not $Path) { return $false }
    return (Test-Path (Join-Path $Path "bin\java.exe"))
}

function Resolve-JdkPath {
    param([string]$ExplicitPath, [string]$ProjectRoot)

    if (Test-JdkPath $ExplicitPath) {
        return $ExplicitPath
    }

    $fromPrompt = Get-JdkFromUserPrompt (Join-Path $ProjectRoot "doc\ia\user-prompt.md")
    if (Test-JdkPath $fromPrompt) {
        return $fromPrompt
    }

    if (Test-JdkPath $env:JAVA_HOME) {
        return $env:JAVA_HOME
    }

    $candidates = @(
        "C:\Users\$env:USERNAME\.jdks\jdk-21",
        "C:\Users\$env:USERNAME\.jdks\ms-21.0.10",
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\openjdk-21"
    )

    foreach ($candidate in $candidates) {
        if (Test-JdkPath $candidate) {
            return $candidate
        }
    }

    return $null
}

function Resolve-MavenCmd {
    param([string]$ExplicitPath)

    if ($ExplicitPath -and (Test-Path $ExplicitPath)) {
        return $ExplicitPath
    }

    # Si IntelliJ esta abierto, intentar derivar Maven desde el ejecutable real.
    $ideaPath = (Get-Process idea64 -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Path)
    if ($ideaPath) {
        $ideaHome = Split-Path (Split-Path $ideaPath -Parent) -Parent
        $fromRunningIdea = Join-Path $ideaHome "plugins\maven\lib\maven3\bin\mvn.cmd"
        if (Test-Path $fromRunningIdea) {
            return $fromRunningIdea
        }
    }

    # Prioridad: Maven integrado de IntelliJ para homogeneizar el entorno del equipo.
    $searchPatterns = @(
        "$env:ProgramFiles\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "${env:ProgramFiles(x86)}\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:LOCALAPPDATA\Programs\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-U\ch-*\*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-C\ch-*\*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-*\ch-*\*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:APPDATA\JetBrains\Toolbox\apps\IDEA-*\ch-*\*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "$env:USERPROFILE\AppData\Local\Programs\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd"
    )

    foreach ($pattern in $searchPatterns) {
        $found = Get-ChildItem -Path $pattern -File -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($found) {
            return $found.FullName
        }
    }

    # Fallback: Maven disponible en PATH.
    $mvnFromPath = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($mvnFromPath) {
        return $mvnFromPath.Source
    }

    return $null
}

$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot

$resolvedJdk = Resolve-JdkPath -ExplicitPath $JdkPath -ProjectRoot $projectRoot
if (-not $resolvedJdk) {
    Write-Error "No se encontro un JDK valido. Usa -JdkPath o define `$env:JAVA_HOME en doc/ia/user-prompt.md."
    exit 1
}

$env:JAVA_HOME = $resolvedJdk
$jdkBin = Join-Path $resolvedJdk "bin"
if (-not (($env:Path -split ';') -contains $jdkBin)) {
    $env:Path = "$jdkBin;$env:Path"
}

$prevErrorPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$javaVersionOutput = & (Join-Path $jdkBin "java.exe") -version 2>&1
$ErrorActionPreference = $prevErrorPreference
$javaVersionText = ($javaVersionOutput | Out-String)
if ($javaVersionText -notmatch 'version\s+"21([\._][0-9]+)?') {
    Write-Error "Se detecto un JDK distinto de la version 21. JAVA_HOME actual: $resolvedJdk"
    exit 1
}

$mvnCmd = Resolve-MavenCmd -ExplicitPath $MavenCmdPath
if (-not $mvnCmd) {
    Write-Error "No se encontro Maven. Usa Maven integrado de IntelliJ o pasa -MavenCmdPath con la ruta a mvn.cmd."
    exit 1
}

Write-Host "JAVA_HOME: $resolvedJdk"
Write-Host "Maven: $mvnCmd"
Write-Host "Ejecutando tests..."

& $mvnCmd "-DskipTests=false" "test"
$exitCode = $LASTEXITCODE

if ($exitCode -eq 0) {
    Write-Host "BUILD SUCCESS. Reportes: target\surefire-reports"
} else {
    Write-Host "BUILD FAILURE. Revisa: target\surefire-reports"
}

exit $exitCode


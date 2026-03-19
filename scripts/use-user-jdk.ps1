param(
    [string]$JdkPath,
    [string]$MavenPath,
    [switch]$RunMaven,
    [switch]$RunMain
)

# Intentar obtener la ruta desde doc/ia/user-prompt.md si no se pasó como parámetro
if (-not $JdkPath -or -not $MavenPath) {
    $candidatePaths = @(
        Join-Path $PSScriptRoot "..\doc\ia\user-prompt.md",
        Join-Path $PSScriptRoot "..\..\doc\ia\user-prompt.md",
        Join-Path $PSScriptRoot "..\..\..\doc\ia\user-prompt.md"
    )
    foreach ($p in $candidatePaths) {
        if (Test-Path $p) {
            $content = Get-Content $p -Raw -ErrorAction SilentlyContinue
            if (-not $JdkPath -and $content -match "\$env:JAVA_HOME\s*=\s*'([^']+)'") {
                $JdkPath = $matches[1]
                $foundUserPromptPath = $p
            }
            if (-not $MavenPath -and $content -match "\$env:MAVEN_HOME\s*=\s*'([^']+)'") {
                $MavenPath = $matches[1]
                $foundUserPromptPath = $p
            }
            if ($JdkPath -and $MavenPath) { break }
        }
    }
}

# Ruta por defecto para escribir/editar user-prompt (si se necesita crear)
if (-not $foundUserPromptPath) {
    $foundUserPromptPath = Join-Path $PSScriptRoot "..\doc\ia\user-prompt.md"
}

function Confirm-YesNo($message) {
    while ($true) {
        $r = Read-Host "$message (S/N)"
        if ($r -match '^[Ss]') { return $true }
        if ($r -match '^[Nn]') { return $false }
        Write-Host "Respuesta no válida. Introduce S o N."
    }
}

# Validación de rutas: comprobar bin\java.exe y bin\mvn.cmd o mvn
function Is-Valid-JDK($path) {
    return (Test-Path (Join-Path $path 'bin\java.exe'))
}
function Is-Valid-Maven($path) {
    return (Test-Path (Join-Path $path 'bin\mvn.cmd') -or Test-Path (Join-Path $path 'bin\mvn'))
}

# Si no hay ruta al JDK o la ruta no existe, pedir confirmación y permitir añadir/editar
if (-not $JdkPath -or -not (Is-Valid-JDK $JdkPath)) {
    Write-Host "La ruta al JDK no se encontró o no existe: $JdkPath"
    $do = Confirm-YesNo "¿Quieres proporcionar una ruta alternativa o crear/actualizar `doc/ia/user-prompt.md` con una ruta válida?"
    if (-not $do) {
        Write-Error "Operación abortada por el usuario. No se modificará el entorno."
        exit 1
    }

    # Pedir la ruta al JDK al usuario
    while ($true) {
        $candidate = Read-Host "Introduce la ruta completa al JDK (por ejemplo: C:\Program Files\Java\jdk-17)"
        if (-not $candidate) { Write-Host "Ruta vacía. Intenta de nuevo."; continue }
        if (Is-Valid-JDK $candidate) {
            $JdkPath = $candidate
            break
        } else {
            Write-Host "La ruta no contiene bin\java.exe o no existe: $candidate"
            $tryAgain = Confirm-YesNo "¿Quieres intentarlo de nuevo?"
            if (-not $tryAgain) { Write-Error "No se proporcionó una ruta válida. Abortando."; exit 1 }
        }
    }

    # Ahora que tenemos una ruta válida, escribirla en user-prompt.md si el usuario confirma
    $write = Confirm-YesNo "¿Deseas añadir o actualizar la entrada en $foundUserPromptPath con esta ruta? (archivo local, no versionado)"
    if ($write) {
        $dir = Split-Path $foundUserPromptPath -Parent
        if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
        if (Test-Path $foundUserPromptPath) {
            $content = Get-Content $foundUserPromptPath -Raw -ErrorAction SilentlyContinue
            if ($content -match "\$env:JAVA_HOME\s*=\s*'([^']+)'") {
                $existing = $matches[1]
                if ($existing -ne $JdkPath) {
                    $replace = Confirm-YesNo "El archivo ya contiene una entrada JAVA_HOME con ruta '$existing'. ¿Deseas reemplazarla por '$JdkPath'?"
                    if ($replace) {
                        $newContent = ($content -replace "(\$env:JAVA_HOME\s*=\s*')([^']+)(')", "`$env:JAVA_HOME = '$JdkPath'")
                        Set-Content -Path $foundUserPromptPath -Value $newContent -Encoding UTF8
                        Write-Host "Entrada reemplazada en $foundUserPromptPath"
                    } else {
                        Write-Host "No se modificó $foundUserPromptPath"
                    }
                } else {
                    Write-Host "La ruta ya está presente en $foundUserPromptPath. No se añade nada."
                }
            } else {
                # No tiene ninguna entrada, append
                Add-Content -Path $foundUserPromptPath -Value "`$env:JAVA_HOME = '$JdkPath'" -Encoding UTF8
                Write-Host "Entrada añadida a $foundUserPromptPath"
            }
        } else {
            # Crear archivo con contenido inicial
            $header = @(
                '# user-prompt (configuración local - NO versionar)',
                '# Añadida automáticamente por scripts/use-user-jdk.ps1',
                '',
                "`$env:JAVA_HOME = '$JdkPath'",
                '`$env:Path = "$env:JAVA_HOME\bin;$env:Path"'
            ) -join "`r`n"
            Set-Content -Path $foundUserPromptPath -Value $header -Encoding UTF8
            Write-Host "Archivo creado: $foundUserPromptPath"
        }
    } else {
        Write-Host "No se añadió la ruta a $foundUserPromptPath. Continúo aplicando la ruta de forma temporal solo a la sesión."
    }
}

# Ahora manejar Maven: si no hay ruta o no es válida, preguntar si desean añadir/confirmar
if (-not $MavenPath -or -not (Is-Valid-Maven $MavenPath)) {
    Write-Host "La ruta a Maven no se encontró o no existe: $MavenPath"
    $doM = Confirm-YesNo "¿Quieres proporcionar una ruta alternativa para Maven o crear/actualizar `doc/ia/user-prompt.md` con una ruta válida?"
    if ($doM) {
        while ($true) {
            $candidateM = Read-Host "Introduce la ruta completa a Maven (por ejemplo: C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven3) (Deja en blanco para omitir)"
            if (-not $candidateM) { Write-Host "Omitido por el usuario."; break }
            if (Is-Valid-Maven $candidateM) {
                $MavenPath = $candidateM
                break
            } else {
                Write-Host "La ruta no contiene bin\mvn.cmd ni bin\mvn o no existe: $candidateM"
                $tryAgainM = Confirm-YesNo "¿Quieres intentarlo de nuevo?"
                if (-not $tryAgainM) { Write-Host "Omitido por el usuario."; break }
            }
        }

        if ($MavenPath) {
            $writeM = Confirm-YesNo "¿Deseas añadir o actualizar la entrada MAVEN_HOME en $foundUserPromptPath con esta ruta? (archivo local, no versionado)"
            if ($writeM) {
                if (Test-Path $foundUserPromptPath) {
                    $content = Get-Content $foundUserPromptPath -Raw -ErrorAction SilentlyContinue
                    if ($content -match "\$env:MAVEN_HOME\s*=\s*'([^']+)'") {
                        $existingM = $matches[1]
                        if ($existingM -ne $MavenPath) {
                            $replaceM = Confirm-YesNo "El archivo ya contiene una entrada MAVEN_HOME con ruta '$existingM'. ¿Deseas reemplazarla por '$MavenPath'?"
                            if ($replaceM) {
                                $newContent = ($content -replace "(\$env:MAVEN_HOME\s*=\s*')([^']+)(')", "`$env:MAVEN_HOME = '$MavenPath'")
                                Set-Content -Path $foundUserPromptPath -Value $newContent -Encoding UTF8
                                Write-Host "Entrada MAVEN_HOME reemplazada en $foundUserPromptPath"
                            } else {
                                Write-Host "No se modificó $foundUserPromptPath"
                            }
                        } else {
                            Write-Host "La ruta MAVEN_HOME ya está presente en $foundUserPromptPath. No se añade nada."
                        }
                    } else {
                        Add-Content -Path $foundUserPromptPath -Value "`$env:MAVEN_HOME = '$MavenPath'" -Encoding UTF8
                        Write-Host "Entrada MAVEN_HOME añadida a $foundUserPromptPath"
                    }
                } else {
                    # Si el archivo no existía, crear con ambas entradas si JdkPath ya estaba presente
                    $header = @(
                        '# user-prompt (configuración local - NO versionar)',
                        '# Añadida automáticamente por scripts/use-user-jdk.ps1',
                        '',
                        "`$env:JAVA_HOME = '$JdkPath'",
                        '`$env:Path = "$env:JAVA_HOME\bin;$env:Path"',
                        "`$env:MAVEN_HOME = '$MavenPath'",
                        '`$env:Path = "$env:MAVEN_HOME\bin;$env:Path"'
                    ) -join "`r`n"
                    Set-Content -Path $foundUserPromptPath -Value $header -Encoding UTF8
                    Write-Host "Archivo creado con MAVEN_HOME: $foundUserPromptPath"
                }
            } else {
                Write-Host "No se añadió MAVEN_HOME a $foundUserPromptPath."
            }
        }
    } else {
        Write-Host "No se proporcionó ruta a Maven. Se omite la configuración de MAVEN_HOME."
    }
}

# Aplicar a la sesión actual del script (asegurarse de no duplicar JAVA_HOME\bin ni MAVEN_HOME\bin en PATH)
if ($JdkPath) {
    $env:JAVA_HOME = $JdkPath
    $binPath = Join-Path $env:JAVA_HOME 'bin'
    $pathParts = $env:Path -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
    if (-not ($pathParts -contains $binPath)) {
        $env:Path = "$binPath;$env:Path"
        Write-Host "Se añadió $binPath al PATH de la sesión (una sola vez)."
    } else {
        Write-Host "$binPath ya está presente en PATH. No se duplicó."
    }
    Write-Host "JAVA_HOME establecido en: $env:JAVA_HOME"
}

if ($MavenPath) {
    $env:MAVEN_HOME = $MavenPath
    $mBin = Join-Path $env:MAVEN_HOME 'bin'
    $pathParts = $env:Path -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
    if (-not ($pathParts -contains $mBin)) {
        $env:Path = "$mBin;$env:Path"
        Write-Host "Se añadió $mBin al PATH de la sesión (una sola vez)."
    } else {
        Write-Host "$mBin ya está presente en PATH. No se duplicó."
    }
    Write-Host "MAVEN_HOME establecido en: $env:MAVEN_HOME"
}

# Mostrar la ruta del ejecutable java de forma segura
try {
    $javaCmd = (Get-Command java -ErrorAction Stop).Source
    Write-Host "Ruta del ejecutable java: $javaCmd"
} catch {
    Write-Warning "No se encontró el ejecutable 'java' en PATH después de ajustar JAVA_HOME"
}

# Mostrar versión de java de forma robusta: usar el operador de llamada y capturar salida
try {
    $versionOutput = & java -version 2>&1
    if ($versionOutput) {
        $versionOutput | ForEach-Object { Write-Host $_ }
    }
} catch {
    Write-Warning "No se pudo ejecutar 'java -version': $_"
}

# Mostrar info de mvn si está presente
if ($MavenPath) {
    try {
        $mvnCmd = (Get-Command mvn -ErrorAction Stop).Source
        Write-Host "Ruta del ejecutable mvn: $mvnCmd"
    } catch {
        Write-Warning "No se encontró el ejecutable 'mvn' en PATH después de ajustar MAVEN_HOME"
    }
    try {
        $mvnOut = & mvn -v 2>&1
        if ($mvnOut) { $mvnOut | ForEach-Object { Write-Host $_ } }
    } catch {
        Write-Warning "No se pudo ejecutar 'mvn -v': $_"
    }
}

# Opciones adicionales
if ($RunMaven) {
    Write-Host "Ejecutando: mvn -DskipTests=true compile"
    mvn -DskipTests=true compile
}

if ($RunMain) {
    Write-Host "Ejecutando la clase Main: es.iesquevedo.Main"
    java -cp target\classes es.iesquevedo.Main
}

Write-Host "Script finalizado. Ten en cuenta que las variables de entorno aplicadas solo existen en esta sesión de PowerShell."

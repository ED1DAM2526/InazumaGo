#!/usr/bin/env powershell
# Script para ejecutar la aplicación InazumaGo

param(
    [switch]$Console,
    [switch]$Help
)

if ($Help) {
    Write-Host "Uso: .\run-app.ps1 [opciones]"
    Write-Host ""
    Write-Host "Opciones:"
    Write-Host "  -Console  Ejecutar en modo CLI (sin GUI)"
    Write-Host "  -Help     Mostrar esta ayuda"
    Write-Host ""
    Write-Host "Ejemplos:"
    Write-Host "  .\run-app.ps1              # Ejecutar en modo GUI"
    Write-Host "  .\run-app.ps1 -Console     # Ejecutar en modo CLI"
    exit 0
}

$ErrorActionPreference = "Stop"

# Verificar que Maven compile ha sido ejecutado
if (-not (Test-Path "target/classes")) {
    Write-Host "❌ Error: target/classes no existe"
    Write-Host ""
    Write-Host "Primero compila el proyecto:"
    Write-Host "  mvn clean compile"
    exit 1
}

# Construir el classpath
$classpath = "target/classes"

# Añadir todos los JARs de target/dependency
if (Test-Path "target/dependency") {
    Get-ChildItem -Path "target/dependency" -Filter "*.jar" | ForEach-Object {
        $classpath += ";$($_.FullName)"
    }
} else {
    Write-Host "⚠️  Advertencia: target/dependency no existe"
    Write-Host "La aplicación puede necesitar más dependencias."
    Write-Host ""
}

# Argumentos
$javaArgs = @(
    "-cp", $classpath,
    "--add-modules", "javafx.controls,javafx.fxml",
    "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
    "es.iesquevedo.Main"
)

if ($Console) {
    Write-Host "Ejecutando en modo CLI..."
    $javaArgs += "console"
} else {
    Write-Host "Ejecutando en modo GUI..."
    Write-Host "Se abrirá una ventana JavaFX..."
}

Write-Host ""

# Ejecutar Java
& java $javaArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Error al ejecutar la aplicación (código: $LASTEXITCODE)"
    exit $LASTEXITCODE
}



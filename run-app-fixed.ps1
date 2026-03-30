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

# Verificar que dependencias han sido descargadas
if (-not (Test-Path "target/dependency")) {
    Write-Host "❌ Error: target/dependency no existe"
    Write-Host ""
    Write-Host "Primero descarga las dependencias:"
    Write-Host "  mvn dependency:copy-dependencies"
    exit 1
}

# Construir lista de archivos JAR
$jars = @(Get-ChildItem -Path "target/dependency" -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)

# Construir el classpath
$classPath = "target/classes" + ";" + ($jars -join ";")

# Argumentos base
$args = @("-cp", $classPath)

# Añadir argumentos de aplicación
if ($Console) {
    Write-Host "Ejecutando en modo CLI..."
    $args += @("es.iesquevedo.Main", "console")
} else {
    Write-Host "Ejecutando en modo GUI..."
    Write-Host "Se abrirá una ventana JavaFX..."
    $args += "es.iesquevedo.Main"
}

Write-Host ""

# Ejecutar Java
& java $args

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Error al ejecutar la aplicación (código: $LASTEXITCODE)"
    exit $LASTEXITCODE
}


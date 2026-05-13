param(
    [string]$FilePath
)

# Leer el archivo como bytes
$bytes = [System.IO.File]::ReadAllBytes($FilePath)

# Verificar si tiene BOM UTF-8 (EF BB BF)
if ($bytes.Count -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    Write-Host "BOM detectado en: $FilePath"
    # Leer el contenido como texto
    $content = [System.IO.File]::ReadAllText($FilePath)
    # Escribir el archivo sin BOM usando UTF-8
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($FilePath, $content, $utf8NoBom)
    Write-Host "BOM eliminado exitosamente de: $FilePath"
} else {
    Write-Host "No se detectó BOM en: $FilePath"
}


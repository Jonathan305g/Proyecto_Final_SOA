param(
    [string]$UsuarioAdministrador = 'administrador',
    [int]$PuertoBaseDatos = 3307
)
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
$archivoLocal = Join-Path $raizProyecto '.local/application.properties'
if (Test-Path -LiteralPath $archivoLocal) {
    Write-Host 'La configuración local ya existe. Se conservó sin cambios.'
    exit 0
}
if ($UsuarioAdministrador -notmatch '^[a-zA-Z0-9._-]{3,60}$') {
    throw 'El usuario debe tener entre 3 y 60 letras, números, puntos o guiones.'
}
# Contraseña aleatoria por instalación. Nunca se incluye en el repositorio.
$bytesClave = New-Object byte[] 24
$generador = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $generador.GetBytes($bytesClave) } finally { $generador.Dispose() }
$claveLocal = [Convert]::ToBase64String($bytesClave)
$textoLocal = @"
# Archivo privado e ignorado por Git. No compartir ni publicar.
app.bootstrap.username=$UsuarioAdministrador
app.bootstrap.password=$claveLocal
spring.datasource.url=jdbc:mysql://127.0.0.1:$PuertoBaseDatos/inventario_bodega?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=America/Guayaquil
spring.datasource.username=root
spring.datasource.password=
"@
New-Item -ItemType Directory -Path (Split-Path -Parent $archivoLocal) -Force | Out-Null
[System.IO.File]::WriteAllText($archivoLocal, $textoLocal, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Configuración creada en $archivoLocal"
Write-Host "Usuario inicial: $UsuarioAdministrador. Consulte la contraseña en ese archivo privado."

<?php
// Herramienta local de XAMPP: recibe claves por stdin y devuelve solamente hashes.
// La aplicación web sigue ejecutándose exclusivamente con Java/Spring Boot.
try {
    $datos = json_decode(stream_get_contents(STDIN), true, 512, JSON_THROW_ON_ERROR);
    $resultado = [];
    foreach (['admin', 'bodeguero'] as $cuenta) {
        $clave = $datos[$cuenta] ?? null;
        if (!is_string($clave) || strlen($clave) < 10 || strlen($clave) > 72) {
            throw new RuntimeException('Longitud de contraseña no válida.');
        }
        $resultado[$cuenta] = password_hash($clave, PASSWORD_BCRYPT, ['cost' => 10]);
    }
    echo json_encode($resultado, JSON_THROW_ON_ERROR);
} catch (Throwable $error) {
    fwrite(STDERR, "No se pudieron generar los hashes BCrypt.\n");
    exit(1);
}

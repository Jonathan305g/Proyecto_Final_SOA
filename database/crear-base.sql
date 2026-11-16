-- OPCIONAL: solo crea la base, para administradores que preparan permisos previamente.
-- La importación completa ya crea la base: importe src/main/resources/schema.sql
-- o .local/importar-inventario.sql para incluir las dos cuentas con claves privadas.
CREATE DATABASE IF NOT EXISTS inventario_bodega CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Única definición del esquema. Hibernate solo valida estas seis tablas.
CREATE TABLE IF NOT EXISTS usuarios (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 username VARCHAR(60) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL,
 rol VARCHAR(20) NOT NULL, estado BIT NOT NULL,
 CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN','BODEGUERO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS categorias (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS marcas (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS proveedores (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(120) NOT NULL,
 telefono VARCHAR(30), correo VARCHAR(160)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS productos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 codigo VARCHAR(50) NOT NULL UNIQUE, nombre VARCHAR(120) NOT NULL, descripcion VARCHAR(500),
 stock_actual INT NOT NULL DEFAULT 0, stock_minimo INT NOT NULL DEFAULT 0,
 categoria_id BIGINT NOT NULL, marca_id BIGINT NOT NULL, activo BIT NOT NULL,
 CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
 CONSTRAINT fk_producto_marca FOREIGN KEY (marca_id) REFERENCES marcas(id),
 CONSTRAINT ck_producto_stock CHECK (stock_actual >= 0 AND stock_minimo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS entradas_inventario (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, fecha DATETIME(6) NOT NULL,
 cantidad INT NOT NULL, observacion VARCHAR(500), producto_id BIGINT NOT NULL,
 proveedor_id BIGINT NOT NULL, usuario_responsable_id BIGINT NOT NULL,
 CONSTRAINT fk_entrada_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
 CONSTRAINT fk_entrada_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
 CONSTRAINT fk_entrada_usuario FOREIGN KEY (usuario_responsable_id) REFERENCES usuarios(id),
 CONSTRAINT ck_entrada_cantidad CHECK (cantidad > 0), INDEX idx_entrada_fecha (fecha,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos iniciales (demo). Idempotente: cada bloque solo inserta si aún no existe la fila.
-- Contraseña de los usuarios seed (login): Secret1234  (hash BCrypt generado con Spring Security).
-- Requiere en application.properties:
--   spring.jpa.defer-datasource-initialization=true
--   spring.sql.init.mode=always

-- Usuario vendedor seed (publica los productos de ejemplo)
INSERT INTO usuarios (nombre, apellido, email, password, fecha_nacimiento, sexo, role)
SELECT 'Seed', 'Vendedor', 'seed-demo@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1990-01-15', 'NO_INDICA', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'seed-demo@local.dev');

-- Usuario admin opcional (mismas credenciales de demo)
INSERT INTO usuarios (nombre, apellido, email, password, fecha_nacimiento, sexo, role)
SELECT 'Seed', 'Admin', 'seed-admin@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1990-01-15', 'NO_INDICA', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'seed-admin@local.dev');

-- Productos asignados al vendedor seed (solo si no existe ya un producto con ese nombre)
INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'Mouse Logitech G203', 'Óptico, 8000 DPI, RGB', 45.99, 20, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Mouse Logitech G203');

INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'Teclado Keychron K2', 'Mecánico 75%, Bluetooth', 120.00, 8, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Teclado Keychron K2');

INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'Monitor LG 27"', 'QHD 144Hz', 380.50, 5, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Monitor LG 27"');

INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'Webcam Logitech C920', '1080p', 89.00, 12, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Webcam Logitech C920');

INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'Auriculares HyperX Cloud II', 'Sonido envolvente 7.1 virtual (software)', 95.00, 15, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Auriculares HyperX Cloud II');

INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT 'SSD Samsung 990 PRO 1TB', 'NVMe PCIe 4.0', 165.00, 6, u.id
FROM usuarios u
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'SSD Samsung 990 PRO 1TB');

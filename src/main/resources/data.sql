-- Datos iniciales (demo). Idempotente: cada bloque solo inserta si aún no existe la fila.
-- Contraseña de los usuarios seed (login): Secret1234  (hash BCrypt generado con Spring Security).
-- En application.properties: spring.jpa.defer-datasource-initialization=true, spring.sql.init.mode=always
-- (para que este script corra después del schema Hibernate).

-- Usuario vendedor seed (publica los productos de ejemplo)
INSERT INTO usuarios (nombre, apellido, nombre_usuario, email, password, fecha_nacimiento, sexo, role)
SELECT 'Seed', 'Vendedor', 'seed-demo',
       'seed-demo@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1990-01-15', 'NO_INDICA', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'seed-demo@local.dev');

-- Usuario admin opcional (mismas credenciales de demo)
INSERT INTO usuarios (nombre, apellido, nombre_usuario, email, password, fecha_nacimiento, sexo, role)
SELECT 'Seed', 'Admin', 'seed-admin',
       'seed-admin@local.dev',
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

-- Comprador seed (carrito, checkout, pagos mock, favoritos)
INSERT INTO usuarios (nombre, apellido, nombre_usuario, email, password, fecha_nacimiento, sexo, role)
SELECT 'Seed', 'Comprador', 'seed-comprador',
       'seed-comprador@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1992-06-01', 'NO_INDICA', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'seed-comprador@local.dev');

-- Vendedores extra (flujo Postman dos cuentas)
INSERT INTO usuarios (nombre, apellido, nombre_usuario, email, password, fecha_nacimiento, sexo, role)
SELECT 'Vendedor', 'A', 'vendedor-a',
       'vendedor-a@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1988-03-10', 'MASCULINO', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'vendedor-a@local.dev');

INSERT INTO usuarios (nombre, apellido, nombre_usuario, email, password, fecha_nacimiento, sexo, role)
SELECT 'Vendedor', 'B', 'vendedor-b',
       'vendedor-b@local.dev',
       '$2a$10$9fnM3/55XDOnL.wcVV4V0eFkCoA//wFrI6W91LgVulnr4SVFsWGN6',
       '1989-04-11', 'FEMENINO', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'vendedor-b@local.dev');

-- Backfill de nombre_usuario para instalaciones previas (si ya existían estos usuarios)
UPDATE usuarios SET nombre_usuario = 'seed-demo'
WHERE email = 'seed-demo@local.dev' AND (nombre_usuario IS NULL OR TRIM(nombre_usuario) = '');
UPDATE usuarios SET nombre_usuario = 'seed-admin'
WHERE email = 'seed-admin@local.dev' AND (nombre_usuario IS NULL OR TRIM(nombre_usuario) = '');
UPDATE usuarios SET nombre_usuario = 'seed-comprador'
WHERE email = 'seed-comprador@local.dev' AND (nombre_usuario IS NULL OR TRIM(nombre_usuario) = '');
UPDATE usuarios SET nombre_usuario = 'vendedor-a'
WHERE email = 'vendedor-a@local.dev' AND (nombre_usuario IS NULL OR TRIM(nombre_usuario) = '');
UPDATE usuarios SET nombre_usuario = 'vendedor-b'
WHERE email = 'vendedor-b@local.dev' AND (nombre_usuario IS NULL OR TRIM(nombre_usuario) = '');

-- Categorías (búsqueda por categoriaId)
INSERT INTO categorias (nombre)
SELECT 'Periféricos'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Periféricos');

INSERT INTO categorias (nombre)
SELECT 'Monitores'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Monitores');

INSERT INTO categorias (nombre)
SELECT 'Almacenamiento'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Almacenamiento');

INSERT INTO categorias (nombre)
SELECT 'Audio y vídeo'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Audio y vídeo');

INSERT INTO categorias (nombre)
SELECT 'Notebooks'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Notebooks');

INSERT INTO categorias (nombre)
SELECT 'Gaming'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Gaming');

-- Relación producto ↔ categoría (para GET /api/productos/search?categoriaId=…)
INSERT INTO productos_categorias (producto_id, categoria_id)
SELECT p.id, c.id
FROM productos p
JOIN categorias c ON c.nombre = 'Periféricos'
WHERE p.nombre IN ('Mouse Logitech G203', 'Teclado Keychron K2', 'Webcam Logitech C920', 'Auriculares HyperX Cloud II')
  AND NOT EXISTS (
    SELECT 1 FROM productos_categorias pc
    WHERE pc.producto_id = p.id AND pc.categoria_id = c.id
  );

INSERT INTO productos_categorias (producto_id, categoria_id)
SELECT p.id, c.id
FROM productos p
JOIN categorias c ON c.nombre = 'Monitores'
WHERE p.nombre = 'Monitor LG 27"'
  AND NOT EXISTS (
    SELECT 1 FROM productos_categorias pc
    WHERE pc.producto_id = p.id AND pc.categoria_id = c.id
  );

INSERT INTO productos_categorias (producto_id, categoria_id)
SELECT p.id, c.id
FROM productos p
JOIN categorias c ON c.nombre = 'Almacenamiento'
WHERE p.nombre = 'SSD Samsung 990 PRO 1TB'
  AND NOT EXISTS (
    SELECT 1 FROM productos_categorias pc
    WHERE pc.producto_id = p.id AND pc.categoria_id = c.id
  );

INSERT INTO productos_categorias (producto_id, categoria_id)
SELECT p.id, c.id
FROM productos p
JOIN categorias c ON c.nombre = 'Gaming'
WHERE p.nombre IN ('Mouse Logitech G203', 'Auriculares HyperX Cloud II')
  AND NOT EXISTS (
    SELECT 1 FROM productos_categorias pc
    WHERE pc.producto_id = p.id AND pc.categoria_id = c.id
  );

-- Imágenes demo por producto (detalle ampliado)
INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/mouse-g203-1/900/700'
FROM productos p
WHERE p.nombre = 'Mouse Logitech G203'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 1, 'https://picsum.photos/seed/mouse-g203-2/900/700'
FROM productos p
WHERE p.nombre = 'Mouse Logitech G203'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 1
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/keychron-k2-1/900/700'
FROM productos p
WHERE p.nombre = 'Teclado Keychron K2'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/monitor-lg-27-1/900/700'
FROM productos p
WHERE p.nombre = 'Monitor LG 27"'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/webcam-c920-1/900/700'
FROM productos p
WHERE p.nombre = 'Webcam Logitech C920'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/hyperx-cloud-ii-1/900/700'
FROM productos p
WHERE p.nombre = 'Auriculares HyperX Cloud II'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://picsum.photos/seed/ssd-990-pro-1/900/700'
FROM productos p
WHERE p.nombre = 'SSD Samsung 990 PRO 1TB'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

-- Dirección del comprador seed
INSERT INTO direcciones (usuario_id, calle, numero, codigo_postal, ciudad, provincia, pais, referencia, principal)
SELECT u.id, 'Av. Corrientes', '1234', 'C1043', 'CABA', 'CABA', 'AR', 'Portería', true
FROM usuarios u
WHERE u.email = 'seed-comprador@local.dev'
  AND NOT EXISTS (
    SELECT 1 FROM direcciones d
    JOIN usuarios u2 ON d.usuario_id = u2.id
    WHERE u2.email = 'seed-comprador@local.dev' AND d.calle = 'Av. Corrientes' AND d.numero = '1234'
  );

-- Favorito: comprador + mouse
INSERT INTO favoritos (usuario_id, producto_id)
SELECT uc.id, p.id
FROM usuarios uc
CROSS JOIN productos p
WHERE uc.email = 'seed-comprador@local.dev'
  AND p.nombre = 'Mouse Logitech G203'
  AND NOT EXISTS (
    SELECT 1 FROM favoritos f
    WHERE f.usuario_id = uc.id AND f.producto_id = p.id
  );

-- Pedido ENTREGADO (historial + posible POST /api/resenas sin reseña previa en BD)
INSERT INTO pedidos (comprador_id, fecha, total, estado, direccion_envio, notas,
                     shipping_calle, shipping_numero, shipping_codigo_postal, shipping_ciudad, shipping_provincia, shipping_pais, shipping_referencia)
SELECT u.id, '2026-01-05 14:00:00', 45.99, 'ENTREGADO', 'Av. Corrientes 1234, CABA', 'SEED_ENTREGADO_MARK',
       'Av. Corrientes', '1234', 'C1043', 'CABA', 'CABA', 'AR', 'Seed SQL'
FROM usuarios u
WHERE u.email = 'seed-comprador@local.dev'
  AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.notas = 'SEED_ENTREGADO_MARK');

INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT pe.id, pr.id, 1, 45.99, 45.99
FROM pedidos pe
JOIN usuarios u ON pe.comprador_id = u.id AND u.email = 'seed-comprador@local.dev'
JOIN productos pr ON pr.nombre = 'Mouse Logitech G203'
WHERE pe.notas = 'SEED_ENTREGADO_MARK'
  AND NOT EXISTS (SELECT 1 FROM pedido_items pi WHERE pi.pedido_id = pe.id);

-- Pedido PENDIENTE_PAGO (POST /api/pagos/mock con pedidoId)
INSERT INTO pedidos (comprador_id, fecha, total, estado, direccion_envio, notas,
                     shipping_calle, shipping_numero, shipping_codigo_postal, shipping_ciudad, shipping_provincia, shipping_pais, shipping_referencia)
SELECT u.id, '2026-01-06 11:00:00', 89.00, 'PENDIENTE_PAGO', 'Av. Corrientes 1234, CABA', 'SEED_PENDIENTE_MARK',
       'Av. Corrientes', '1234', 'C1043', 'CABA', 'CABA', 'AR', 'Seed SQL'
FROM usuarios u
WHERE u.email = 'seed-comprador@local.dev'
  AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.notas = 'SEED_PENDIENTE_MARK');

INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT pe.id, pr.id, 1, 89.00, 89.00
FROM pedidos pe
JOIN usuarios u ON pe.comprador_id = u.id AND u.email = 'seed-comprador@local.dev'
JOIN productos pr ON pr.nombre = 'Webcam Logitech C920'
WHERE pe.notas = 'SEED_PENDIENTE_MARK'
  AND NOT EXISTS (SELECT 1 FROM pedido_items pi WHERE pi.pedido_id = pe.id);

-- Pedido adicional ya reseñado (para poblar listados de reseñas sin bloquear el flujo demo principal)
INSERT INTO pedidos (comprador_id, fecha, total, estado, direccion_envio, notas,
                     shipping_calle, shipping_numero, shipping_codigo_postal, shipping_ciudad, shipping_provincia, shipping_pais, shipping_referencia)
SELECT u.id, '2026-01-04 10:30:00', 120.00, 'ENTREGADO', 'Av. Corrientes 1234, CABA', 'SEED_ENTREGADO_RESENADO_MARK',
       'Av. Corrientes', '1234', 'C1043', 'CABA', 'CABA', 'AR', 'Seed SQL'
FROM usuarios u
WHERE u.email = 'seed-comprador@local.dev'
  AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.notas = 'SEED_ENTREGADO_RESENADO_MARK');

INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT pe.id, pr.id, 1, 120.00, 120.00
FROM pedidos pe
JOIN usuarios u ON pe.comprador_id = u.id AND u.email = 'seed-comprador@local.dev'
JOIN productos pr ON pr.nombre = 'Teclado Keychron K2'
WHERE pe.notas = 'SEED_ENTREGADO_RESENADO_MARK'
  AND NOT EXISTS (SELECT 1 FROM pedido_items pi WHERE pi.pedido_id = pe.id);

INSERT INTO resenas (comprador_id, pedido_item_id, puntuacion, comentario, fecha)
SELECT u.id, pi.id, 5, 'Excelente teclado, buena calidad de construcción.', '2026-01-05 09:00:00'
FROM pedido_items pi
JOIN pedidos pe ON pe.id = pi.pedido_id AND pe.notas = 'SEED_ENTREGADO_RESENADO_MARK'
JOIN usuarios u ON u.id = pe.comprador_id
WHERE NOT EXISTS (SELECT 1 FROM resenas r WHERE r.pedido_item_id = pi.id);

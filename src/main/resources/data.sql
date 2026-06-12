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
SELECT 'Perifericos'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Perifericos');

INSERT INTO categorias (nombre)
SELECT 'Monitores'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Monitores');

INSERT INTO categorias (nombre)
SELECT 'Almacenamiento'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Almacenamiento');

INSERT INTO categorias (nombre)
SELECT 'Audio y video'
WHERE NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = 'Audio y video');

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
JOIN categorias c ON c.nombre = 'Perifericos'
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
SELECT p.id, 0, 'https://loremflickr.com/900/700/gaming,mouse?lock=203'
FROM productos p
WHERE p.nombre = 'Mouse Logitech G203'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 1, 'https://loremflickr.com/900/700/computer,mouse?lock=1203'
FROM productos p
WHERE p.nombre = 'Mouse Logitech G203'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 1
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://loremflickr.com/900/700/mechanical,keyboard?lock=202'
FROM productos p
WHERE p.nombre = 'Teclado Keychron K2'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://loremflickr.com/900/700/computer,monitor?lock=201'
FROM productos p
WHERE p.nombre = 'Monitor LG 27"'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://loremflickr.com/900/700/webcam,camera?lock=204'
FROM productos p
WHERE p.nombre = 'Webcam Logitech C920'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://loremflickr.com/900/700/gaming,headphones?lock=205'
FROM productos p
WHERE p.nombre = 'Auriculares HyperX Cloud II'
  AND NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
  );

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, 'https://loremflickr.com/900/700/ssd,drive?lock=206'
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

-- Productos demo extra: asegura al menos 4 productos por categoria para probar filtros
INSERT INTO productos (nombre, descripcion, precio, stock, vendedor_id)
SELECT seed.nombre, seed.descripcion, seed.precio, seed.stock, u.id
FROM usuarios u
JOIN (
    SELECT 'Monitor Samsung Odyssey G5' nombre, 'Monitor curvo QHD 165Hz' descripcion, 420.00 precio, 7 stock
    UNION ALL SELECT 'Monitor Dell UltraSharp 24', 'Panel IPS FHD para oficina', 260.00, 10
    UNION ALL SELECT 'Monitor BenQ GW2480', 'Monitor 24 pulgadas IPS', 185.00, 14
    UNION ALL SELECT 'SSD Kingston NV2 1TB', 'NVMe PCIe 4.0 para notebook y PC', 92.00, 18
    UNION ALL SELECT 'Disco WD Blue 2TB', 'HDD SATA 3.5 pulgadas', 78.00, 9
    UNION ALL SELECT 'Pendrive SanDisk Ultra 128GB', 'USB 3.0 compacto', 18.50, 35
    UNION ALL SELECT 'Parlantes Edifier R1280T', 'Parlantes 2.0 de escritorio', 140.00, 8
    UNION ALL SELECT 'Microfono Blue Yeti', 'Microfono USB para streaming', 155.00, 6
    UNION ALL SELECT 'Camara Sony ZV-1F', 'Camara compacta para video', 640.00, 4
    UNION ALL SELECT 'Barra de sonido JBL Bar 2.0', 'Soundbar compacta Bluetooth', 120.00, 11
    UNION ALL SELECT 'Notebook Lenovo IdeaPad 3', 'Ryzen 5, 16GB RAM, 512GB SSD', 780.00, 5
    UNION ALL SELECT 'Notebook HP Pavilion 15', 'Intel i5, 8GB RAM, 512GB SSD', 820.00, 4
    UNION ALL SELECT 'MacBook Air M1', '13 pulgadas, 8GB RAM, 256GB SSD', 1050.00, 3
    UNION ALL SELECT 'Notebook ASUS VivoBook 14', 'Intel i3, 8GB RAM, 256GB SSD', 610.00, 7
    UNION ALL SELECT 'Joystick Xbox Series', 'Control inalambrico para PC y consola', 85.00, 16
    UNION ALL SELECT 'Mouse Razer DeathAdder V2', 'Mouse gamer ergonomico 20000 DPI', 70.00, 13
) seed
WHERE u.email = 'seed-demo@local.dev'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = seed.nombre);

INSERT INTO productos_categorias (producto_id, categoria_id)
SELECT p.id, c.id
FROM productos p
JOIN (
    SELECT 'Monitor Samsung Odyssey G5' producto, 'Monitores' categoria
    UNION ALL SELECT 'Monitor Dell UltraSharp 24', 'Monitores'
    UNION ALL SELECT 'Monitor BenQ GW2480', 'Monitores'
    UNION ALL SELECT 'SSD Kingston NV2 1TB', 'Almacenamiento'
    UNION ALL SELECT 'Disco WD Blue 2TB', 'Almacenamiento'
    UNION ALL SELECT 'Pendrive SanDisk Ultra 128GB', 'Almacenamiento'
    UNION ALL SELECT 'Parlantes Edifier R1280T', 'Audio y video'
    UNION ALL SELECT 'Microfono Blue Yeti', 'Audio y video'
    UNION ALL SELECT 'Camara Sony ZV-1F', 'Audio y video'
    UNION ALL SELECT 'Barra de sonido JBL Bar 2.0', 'Audio y video'
    UNION ALL SELECT 'Notebook Lenovo IdeaPad 3', 'Notebooks'
    UNION ALL SELECT 'Notebook HP Pavilion 15', 'Notebooks'
    UNION ALL SELECT 'MacBook Air M1', 'Notebooks'
    UNION ALL SELECT 'Notebook ASUS VivoBook 14', 'Notebooks'
    UNION ALL SELECT 'Joystick Xbox Series', 'Gaming'
    UNION ALL SELECT 'Mouse Razer DeathAdder V2', 'Gaming'
) seed ON seed.producto = p.nombre
JOIN categorias c ON c.nombre = seed.categoria
WHERE NOT EXISTS (
    SELECT 1 FROM productos_categorias pc
    WHERE pc.producto_id = p.id AND pc.categoria_id = c.id
);

INSERT INTO producto_imagenes (producto_id, orden_visual, url)
SELECT p.id, 0, seed.url
FROM productos p
JOIN (
    SELECT 'Monitor Samsung Odyssey G5' producto, 'https://loremflickr.com/900/700/gaming,monitor?lock=301' url
    UNION ALL SELECT 'Monitor Dell UltraSharp 24', 'https://loremflickr.com/900/700/office,monitor?lock=302'
    UNION ALL SELECT 'Monitor BenQ GW2480', 'https://loremflickr.com/900/700/desktop,monitor?lock=303'
    UNION ALL SELECT 'SSD Kingston NV2 1TB', 'https://loremflickr.com/900/700/ssd,storage?lock=304'
    UNION ALL SELECT 'Disco WD Blue 2TB', 'https://loremflickr.com/900/700/hard,drive?lock=305'
    UNION ALL SELECT 'Pendrive SanDisk Ultra 128GB', 'https://loremflickr.com/900/700/usb,flash,drive?lock=306'
    UNION ALL SELECT 'Parlantes Edifier R1280T', 'https://loremflickr.com/900/700/bookshelf,speakers?lock=307'
    UNION ALL SELECT 'Microfono Blue Yeti', 'https://loremflickr.com/900/700/usb,microphone?lock=308'
    UNION ALL SELECT 'Camara Sony ZV-1F', 'https://loremflickr.com/900/700/vlogging,camera?lock=309'
    UNION ALL SELECT 'Barra de sonido JBL Bar 2.0', 'https://loremflickr.com/900/700/soundbar,speaker?lock=310'
    UNION ALL SELECT 'Notebook Lenovo IdeaPad 3', 'https://loremflickr.com/900/700/laptop,notebook?lock=311'
    UNION ALL SELECT 'Notebook HP Pavilion 15', 'https://loremflickr.com/900/700/windows,laptop?lock=312'
    UNION ALL SELECT 'MacBook Air M1', 'https://loremflickr.com/900/700/macbook,laptop?lock=313'
    UNION ALL SELECT 'Notebook ASUS VivoBook 14', 'https://loremflickr.com/900/700/notebook,computer?lock=314'
    UNION ALL SELECT 'Joystick Xbox Series', 'https://loremflickr.com/900/700/xbox,controller?lock=315'
    UNION ALL SELECT 'Mouse Razer DeathAdder V2', 'https://loremflickr.com/900/700/gaming,mouse?lock=316'
) seed ON seed.producto = p.nombre
WHERE NOT EXISTS (
    SELECT 1 FROM producto_imagenes pi
    WHERE pi.producto_id = p.id AND pi.orden_visual = 0
);

-- Actualiza imagenes demo existentes para que coincidan con el tipo de producto.
UPDATE producto_imagenes pi
JOIN productos p ON p.id = pi.producto_id
SET pi.url = CASE
    WHEN p.nombre = 'Mouse Logitech G203' AND pi.orden_visual = 0 THEN 'https://loremflickr.com/900/700/gaming,mouse?lock=203'
    WHEN p.nombre = 'Mouse Logitech G203' AND pi.orden_visual = 1 THEN 'https://loremflickr.com/900/700/computer,mouse?lock=1203'
    WHEN p.nombre = 'Teclado Keychron K2' THEN 'https://loremflickr.com/900/700/mechanical,keyboard?lock=202'
    WHEN p.nombre = 'Monitor LG 27"' THEN 'https://loremflickr.com/900/700/computer,monitor?lock=201'
    WHEN p.nombre = 'Webcam Logitech C920' THEN 'https://loremflickr.com/900/700/webcam,camera?lock=204'
    WHEN p.nombre = 'Auriculares HyperX Cloud II' THEN 'https://loremflickr.com/900/700/gaming,headphones?lock=205'
    WHEN p.nombre = 'SSD Samsung 990 PRO 1TB' THEN 'https://loremflickr.com/900/700/ssd,drive?lock=206'
    WHEN p.nombre = 'Monitor Samsung Odyssey G5' THEN 'https://loremflickr.com/900/700/gaming,monitor?lock=301'
    WHEN p.nombre = 'Monitor Dell UltraSharp 24' THEN 'https://loremflickr.com/900/700/office,monitor?lock=302'
    WHEN p.nombre = 'Monitor BenQ GW2480' THEN 'https://loremflickr.com/900/700/desktop,monitor?lock=303'
    WHEN p.nombre = 'SSD Kingston NV2 1TB' THEN 'https://loremflickr.com/900/700/ssd,storage?lock=304'
    WHEN p.nombre = 'Disco WD Blue 2TB' THEN 'https://loremflickr.com/900/700/hard,drive?lock=305'
    WHEN p.nombre = 'Pendrive SanDisk Ultra 128GB' THEN 'https://loremflickr.com/900/700/usb,flash,drive?lock=306'
    WHEN p.nombre = 'Parlantes Edifier R1280T' THEN 'https://loremflickr.com/900/700/bookshelf,speakers?lock=307'
    WHEN p.nombre = 'Microfono Blue Yeti' THEN 'https://loremflickr.com/900/700/usb,microphone?lock=308'
    WHEN p.nombre = 'Camara Sony ZV-1F' THEN 'https://loremflickr.com/900/700/vlogging,camera?lock=309'
    WHEN p.nombre = 'Barra de sonido JBL Bar 2.0' THEN 'https://loremflickr.com/900/700/soundbar,speaker?lock=310'
    WHEN p.nombre = 'Notebook Lenovo IdeaPad 3' THEN 'https://loremflickr.com/900/700/laptop,notebook?lock=311'
    WHEN p.nombre = 'Notebook HP Pavilion 15' THEN 'https://loremflickr.com/900/700/windows,laptop?lock=312'
    WHEN p.nombre = 'MacBook Air M1' THEN 'https://loremflickr.com/900/700/macbook,laptop?lock=313'
    WHEN p.nombre = 'Notebook ASUS VivoBook 14' THEN 'https://loremflickr.com/900/700/notebook,computer?lock=314'
    WHEN p.nombre = 'Joystick Xbox Series' THEN 'https://loremflickr.com/900/700/xbox,controller?lock=315'
    WHEN p.nombre = 'Mouse Razer DeathAdder V2' THEN 'https://loremflickr.com/900/700/gaming,mouse?lock=316'
    ELSE pi.url
END
WHERE pi.orden_visual IN (0, 1)
  AND p.nombre IN (
    'Mouse Logitech G203',
    'Teclado Keychron K2',
    'Monitor LG 27"',
    'Webcam Logitech C920',
    'Auriculares HyperX Cloud II',
    'SSD Samsung 990 PRO 1TB',
    'Monitor Samsung Odyssey G5',
    'Monitor Dell UltraSharp 24',
    'Monitor BenQ GW2480',
    'SSD Kingston NV2 1TB',
    'Disco WD Blue 2TB',
    'Pendrive SanDisk Ultra 128GB',
    'Parlantes Edifier R1280T',
    'Microfono Blue Yeti',
    'Camara Sony ZV-1F',
    'Barra de sonido JBL Bar 2.0',
    'Notebook Lenovo IdeaPad 3',
    'Notebook HP Pavilion 15',
    'MacBook Air M1',
    'Notebook ASUS VivoBook 14',
    'Joystick Xbox Series',
    'Mouse Razer DeathAdder V2'
  );

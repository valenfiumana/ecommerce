# Tareas — Marketplace (tipo Mercado Libre, simplificado)
---

## Epic: Compras y catálogo “real”

### 1. Publicaciones = oferta de un vendedor (DONE Valen)

**Objetivo**  
En un marketplace, un producto no “flota solo”: alguien lo publica y es responsable del stock y del precio.

**Qué hacer**

- Agregar en el modelo `Producto` una relación al `Usuario` que lo publica (ej. `vendedor` / `publicadoPor`).
- Al crear un producto, asignar automáticamente el vendedor = usuario autenticado (no confiar en el body del cliente).
- Definir reglas: solo el vendedor (o un admin) puede editar/borrar sus publicaciones; un comprador solo lee y compra.

**Implementación sugerida (CRUD producto / capas)**

- **Modelo:** entidad `Producto` (o `Product`) + tabla `products` / `productos`; colecciones `@ElementCollection` → tablas tipo `product_images`, `product_sizes` (si aplica mismo diseño).
- **Persistencia:** `ProductoRepository extends JpaRepository<Producto, Long>`.
- **Contrato API:** `ProductRequestDTO`, `ProductResponseDTO` (Bean Validation en el request).
- **Mapeo:** `ProductMapper` — `toEntity`, `toResponseDTO`, `updateEntityFromDTO` (PUT).
- **Lógica:** `ProductService` — `findAll`, `findById`, `save`, `deleteById` (delegación al repo + reglas extra de vendedor/admin).
- **HTTP:** `ProductoController` bajo `/api/productos`: `GET /` listar, `GET /{id}` detalle, `POST /` crear, `PUT /{id}` actualizar, `DELETE /{id}` borrar; `ResponseEntity`; **404** con `ResourceNotFoundException` si el id no existe.
- **Seguridad:** en `SecurityConfig`, alinear con las reglas de arriba (p. ej. GET catálogo público; mutaciones solo vendedor dueño y/o `ROLE_ADMIN`, no confiar en IDs del body para el vendedor).
- **Datos de prueba (opcional):** `data.sql` con inserts a productos, tallas, imágenes si usan init SQL.
- **Tests (opcional):** `ProductServiceTest`, `ProductControllerIntegrationTest` (incluir seguridad en rutas restringidas).

**Criterios de aceptación**

- POST producto guarda el `id` del usuario logueado como vendedor.
- PUT/DELETE de un producto falla con **403** si el usuario no es el vendedor ni admin.
- GET público sigue mostrando catálogo sin exponer datos sensibles del vendedor (solo lo necesario).

> no mandar `vendedorId` en el JSON si puede falsificarse; obtener el usuario del **token** / `SecurityContext`.

---

### 2. Carrito (sesión o usuario) (DONE Valen)

**Objetivo**  
El usuario arma la compra antes de pagar, sin comprometer stock definitivo hasta el checkout (según cómo lo definan).

**Qué hacer**

- Modelo `Carrito` (1 por usuario logueado).
- Modelo `CarritoItem`: producto, cantidad.
- Endpoints: agregar ítem, actualizar cantidad, eliminar ítem, ver carrito.
- Validar stock disponible al agregar/actualizar (no permitir cantidad > stock).

**Implementación sugerida (carrito / capas — flujo tipo `CartItem`)**

- **Modelo:** `CartItem` (`cart_items`): usuario, producto, `quantity` (equivalente a `CarritoItem` + `Carrito` si separan 1:N).
- **Persistencia:** `CartItemRepository` — `findByUserId`, `findByUserIdAndProductId`.
- **Contrato API:** `CartItemRequestDTO`, `CartItemResponseDTO`.
- **Mapeo:** `CartItemMapper`.
- **Lógica:** `CartItemService` — listar por usuario; añadir (usuario/producto existentes, stock); actualizar cantidad (merge/upsert; cantidad ≤ 0 puede eliminar según definición); borrar ítem validando dueño.
- **HTTP:** `CartItemController` `/api/cart`: `GET /` carrito del autenticado, `POST /` agregar, `PUT /` actualizar cantidad (body con `productId` + `quantity`), `DELETE /{id}` quitar línea.
- **Seguridad:** JWT obligatorio; usuario desde `Authentication` (no mezclar con rutas públicas de catálogo).
- **Errores:** `BusinessRuleException` (stock), `ResourceNotFoundException` (usuario/producto/ítem).
- **Tests (opcional):** `CartItemServiceTest` (stock límite, upsert); `CartItemControllerIntegrationTest` con JWT.

**Criterios de aceptación**

- No se puede poner en el carrito más unidades de las que hay en stock.
- Si el producto se elimina o queda sin stock, el carrito refleja error o limpia ese ítem (definir regla y documentarla).

> carrito es solo para usuarios logueados

---

### 3. Checkout / pedido(JULI)

**Objetivo**  
Convertir el carrito en un compromiso de compra con **snapshot de precios** (el precio del producto puede cambiar después).

**Qué hacer**

- Entidad `Pedido`: comprador, fecha, total, estado, dirección de envío (cuando exista), etc.
- Entidad `PedidoItem` (o `LineaPedido`): pedido, producto (referencia), cantidad, **`precioUnitario` guardado al momento de compra**, subtotal.
- Flujo: desde carrito → “confirmar pedido” crea `Pedido` + líneas y luego vacía el carrito (o marca carrito como convertido).

**Implementación sugerida (checkout / capas)**

- **Modelo:** `Pedido` (comprador, fechas, `total`, `estado`, `costoEnvio` si ya aplica tarea 9, FK o snapshot de dirección); `PedidoItem` / `LineaPedido` (FK `pedido`, FK `producto`, `cantidad`, **`precioUnitario`** snapshot, `subtotal` calculado o persistido).
- **Persistencia:** `PedidoRepository`, `PedidoItemRepository` (o cascade desde `Pedido`); queries por comprador para historial (tarea 6).
- **Contrato API:** `CheckoutRequestDTO` (ej. `direccionId` opcional, notas), `PedidoResponseDTO`, `PedidoItemResponseDTO` (sin datos sensibles de más).
- **Mapeo:** `PedidoMapper` (entidad ↔ DTO; líneas anidadas).
- **Lógica:** `CheckoutService` o método en `PedidoService` — `@Transactional` `confirmarDesdeCarrito(usuarioActual)`: leer ítems del carrito, resolver precios actuales del producto y **copiarlos** a cada línea, validar stock (tarea 5), crear `Pedido` + ítems, persistir total, vaciar carrito o marcarlo convertido.
- **HTTP:** `POST /api/pedidos/checkout` (o `POST /api/checkout`); `GET /api/pedidos/{id}` detalle solo si comprador, vendedor involucrado o admin.
- **Seguridad:** JWT; el comprador es siempre el usuario del token (no aceptar `compradorId` del cliente).
- **Errores:** `ResourceNotFoundException` (carrito vacío, producto), `BusinessRuleException` (stock, estado), `ResponseEntity` coherentes.
- **Tests (opcional):** `CheckoutServiceTest` con carrito con 2 ítems y precios distintos del catálogo actual para verificar snapshot; test de carrito vacío.

**Criterios de aceptación**

- El total del pedido coincide con la suma de (precioUnitario × cantidad) de las líneas.
- Si mañana sube el precio del producto, los pedidos viejos siguen mostrando el precio que pagaron.

>el precio snapshot es clave en e-commerce; no recalcular siempre desde el `Producto` actual para pedidos ya cerrados.

---

### 4. Estados de pedido (flujo simple)(JULI)

**Objetivo**  
Todos entienden en qué etapa está la compra (como en ML: pagaste, enviaron, llegó).

**Qué hacer**

- Enum o tabla de estados: por ejemplo `PENDIENTE_PAGO` → `PAGADO` → `ENVIADO` → `ENTREGADO`, y `CANCELADO` donde aplique.
- Transiciones controladas: no pasar de `ENTREGADO` a `PAGADO`, etc.
- Endpoints o acciones: quién puede cambiar el estado (comprador vs vendedor vs sistema de pago).

**Implementación sugerida (estados / capas)**

- **Modelo:** `EstadoPedido` como `enum` en `Pedido` (o entidad catálogo si lo prefieren versionable).
- **Lógica:** un solo lugar que valide transiciones, p. ej. `PedidoService.cambiarEstado(pedidoId, nuevoEstado, actor)` o dominio `pedido.transicionarA(nuevo)`; matriz documentada *desde → hacia* y rol requerido.
- **Persistencia:** `PedidoRepository.getReferenceById` + save tras validar; opcional historial `PedidoEstadoHistorial` si quieren auditoría.
- **Contrato API:** `CambioEstadoRequestDTO` con `nuevoEstado` (y opcional `motivo`).
- **HTTP:** `PATCH /api/pedidos/{id}/estado` o recursos tipo `POST /api/pedidos/{id}/acciones/marcar-enviado` (elegir convención y documentarla).
- **Seguridad:** reglas por rol (comprador solo algunas, vendedor otras, admin todas, “sistema”/mock interno para pago).
- **Errores:** transición inválida → **400** + cuerpo claro (`BusinessRuleException` / `IllegalStateException` mapeada en `GlobalExceptionHandler`).
- **Tests (opcional):** tabla parametrizada *estado actual → nuevo → esperado OK/400*.

**Criterios de aceptación**

- Lista documentada de transiciones válidas.
- Intentar una transición inválida devuelve **400** con mensaje claro.

> centralizar en un método del servicio, ej. `pedido.cambiarEstado(nuevo)`, que valide la transición en un solo lugar.

---

### 5. Stock (Marian In Progress)

**Objetivo**  
No vender lo que no hay; con varios usuarios a la vez, evitar “overselling”.

**Qué hacer**

- Decidir **cuándo** baja el stock: al crear el pedido, al marcar `PAGADO`, etc. (debe alinearse con la tarea de pago mock).
- Al confirmar: verificar stock, descontar, y si falla (race condition), devolver error y no dejar el pedido inconsistente.
- Opción avanzada: bloqueo optimista (`@Version` en JPA) o `UPDATE ... WHERE stock >= cantidad`.

**Implementación sugerida (stock / capas)**

- **Modelo:** campo numérico `stock` (o `cantidadDisponible`) en `Producto`; opcional `@Version Long version` para **bloqueo optimista** en la misma entidad.
- **Persistencia:** además de JPA estándar, considerar `@Modifying @Query("UPDATE Producto p SET p.stock = p.stock - :q WHERE p.id = :id AND p.stock >= :q")` para descuento **atómico**; verificar `updatedRows == 1` o lanzar excepción de negocio.
- **Lógica:** encapsular en `StockService` o métodos privados usados solo desde `CheckoutService` / `PagoService` (tareas 3 y 7) para no esparcir reglas; documentar en código **en qué evento** baja el stock (crear pedido vs `PAGADO`).
- **Transacciones:** mismo `@Transactional` que confirma pedido o aprueba pago; rollback si falla descuento.
- **Tests (recomendado):** al menos un test de servicio que simule dos hilos o dos llamadas secuenciales con stock límite.

**Criterios de aceptación**

- Dos requests simultáneas no dejan stock negativo.
- Si no alcanza el stock, no se crea pedido pago completo (definir rollback).

> transacción + leer stock + verificar + descontar en el mismo `@Transactional`; después mejorar si piden concurrencia.

---

### 6. Historial: “mis compras” y “mis ventas” (Caro DONE)

**Objetivo**  
Cada rol ve lo que le corresponde.

**Qué hacer**

- Endpoint tipo `GET /api/pedidos/mis-compras` → pedidos donde `comprador` = usuario actual.
- Endpoint tipo `GET /api/pedidos/mis-ventas` → pedidos que incluyen líneas de productos cuyo `vendedor` = usuario actual (o pedidos asignados al vendedor, según el modelo).
- Paginación simple (`page`, `size`) recomendada.

**Implementación sugerida (historial / capas)**

- **Persistencia:** `PedidoRepository` — `Page<Pedido> findByCompradorId(Long compradorId, Pageable)`; para ventas: `@Query` con `JOIN` líneas → producto → `vendedor.id = :userId` **distinct** o proyección DTO para evitar duplicados según modelo.
- **Contrato API:** `PedidoSummaryResponseDTO` (id, fecha, total, estado, cantidad ítems); detalle opcional reutilizando DTOs de tarea 3.
- **Mapeo:** `PedidoMapper.toSummaryDto`.
- **Lógica:** `PedidoService.listarMisCompras`, `listarMisVentas` — siempre filtrar por `Authentication`; no aceptar `userId` arbitrario en query salvo admin con control explícito.
- **HTTP:** `GET /api/pedidos/mis-compras?page=&size=&sort=`; `GET /api/pedidos/mis-ventas?...` (misma paginación `Pageable` de Spring).
- **Seguridad:** JWT; denegar listados cruzados (**403**).
- **Errores:** listas vacías → **200** + página vacía.
- **Tests (opcional):** usuario A no obtiene pedidos de B en ambos endpoints.

**Criterios de aceptación**

- Un usuario no ve pedidos de otro.
- Vendedor ve ventas de sus publicaciones; comprador ve sus compras.

---

## Epic: Pagos y envíos (simplificados) 

### 7. Pago mock o sandbox (Rama In Progress)

**Objetivo**  
Cerrar el flujo sin integrar bancos al principio; después reemplazar por Mercado Pago si quieren.

**Qué hacer — fase 1 (mock)**

- Endpoint que “procesa” el pago del pedido: body con `pedidoId` y quizá `resultado: APROBADO | RECHAZADO` simulado, o random controlado por entorno `dev`.
- Si aprobado: estado `PAGADO` y (si definieron así) descuento de stock.
- Si rechazado: pedido queda `PENDIENTE_PAGO` o `CANCELADO`.

**Qué hacer — fase 2 (opcional MP)**

- Crear preferencia de pago, URL de retorno, webhook que actualice el pedido cuando MP notifique.

**Implementación sugerida (pago mock / capas)**

- **Contrato API:** `PagoMockRequestDTO` (`pedidoId`, opcional `resultado: APROBADO | RECHAZADO`); respuesta con `PedidoResponseDTO` o resumen de estado.
- **Lógica:** `PagoService.procesarMock` / `PedidoService.aplicarResultadoPago` — validar que el pedido exista, estado permita pago, **comprador** sea el usuario autenticado; si aprobado: transición a `PAGADO`, opcional descuento de stock (tarea 5); si rechazado: dejar `PENDIENTE_PAGO` o `CANCELADO` según regla.
- **HTTP:** `POST /api/pagos/mock` o `POST /api/pedidos/{id}/pagar-mock` (una sola convención en el equipo).
- **Seguridad:** JWT + comprobación de dueño; opcional restringir endpoint a perfil `dev` / propiedad `app.payments.mock-enabled`.
- **Errores:** `ResourceNotFoundException`, **403** si no es su pedido, `ConflictException` si el estado no admite pago, `BusinessRuleException` para reglas de negocio.
- **Tests:** mock aprobado / rechazado; intento de pago del pedido ajeno → **403**.
- **Fase 2 (Mercado Pago, opcional):** preferencia/checkout API MP; webhook `POST` con validación de firma/cabeceras MP; persistir `paymentId` / `preferenceId` en `Pedido`; handler **idempotente** (MP puede reenviar el mismo evento).

**Criterios de aceptación**

- Flujo feliz y flujo rechazado cubiertos.
- Nadie puede “pagar” el pedido de otro usuario.

>  el mock debe quedar claramente solo para desarrollo (comentario en código o perfil Spring `dev`).

---

### 8. Direcciones de envío (CRUD + checkout) (Rama In Progress)

**Objetivo**  
Saber a dónde mandar el paquete (aunque el envío sea ficticio).

**Qué hacer**

- `Direccion` asociada al `Usuario` (ya tienen entidad; completar campos: calle, número, CP, ciudad, etc.).
- CRUD: crear/listar/editar/borrar **solo las propias**.
- En checkout: elegir `direccionId` existente del usuario y guardarla en el `Pedido` (o copiar texto snapshot por si borra la dirección después).

**Implementación sugerida (direcciones / capas)**

- **Modelo:** `Direccion` con FK `usuario` (`ManyToOne`); campos: calle, número, CP, ciudad, provincia, país, referencia opcional, `principal` boolean si lo usan.
- **Persistencia:** `DireccionRepository` — `List<Direccion> findByUsuarioId(Long usuarioId)`; `existsByIdAndUsuarioId` para validar propiedad.
- **Contrato API:** `DireccionRequestDTO` (Bean Validation), `DireccionResponseDTO` (sin datos de otros usuarios).
- **Mapeo:** `DireccionMapper`.
- **Lógica:** `DireccionService` — CRUD solo si `direccion.usuario.id == usuarioActual`; en checkout (tarea 3) validar `direccionId` pertenece al comprador antes de asociar o copiar snapshot (`@Embeddable` `DireccionSnapshot` en `Pedido`).
- **HTTP:** `DireccionController` `/api/direcciones`: `GET /` propias, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`.
- **Seguridad:** JWT; **403** al tocar ID de dirección ajena.
- **Errores:** `ResourceNotFoundException`; opcional `ConflictException` si intentan borrar dirección usada en pedido sin política definida.
- **Tests (opcional):** `DireccionControllerIntegrationTest` — CRUD ajeno → **403**; checkout con `direccionId` de otro → error.

**Criterios de aceptación**

- No puedo usar la dirección de otro usuario en mi pedido.
- El pedido guarda la dirección usada al momento de la compra (snapshot o FK con regla clara).

---

### 9. Costo de envío (mock)

**Objetivo**  
El total del pedido = productos + envío, sin integrar correo real.

**Qué hacer**

- Regla simple: monto fijo, o por “zona” (enum), o por monto del carrito.
- Guardar `costoEnvio` en el `Pedido` al confirmar.

**Implementación sugerida (envío mock / capas)**

- **Lógica:** clase `EnvioService` o método estático `calcularCosto(BigDecimal subtotalProductos, ZonaEnvio zona, ...)` — regla única (fijo / por enum zona / por tramos de monto); sin tabla obligatoria si es solo configuración (`@ConfigurationProperties` o constantes documentadas).
- **Integración:** invocar desde `CheckoutService` al armar el `Pedido`; persistir `costoEnvio` en el mismo `Pedido` y recalcular `total = subtotalLineas + costoEnvio`.
- **Contrato API (opcional):** `GET /api/envio/cotizar?subtotal=&zona=` si el front quiere preview antes del POST checkout.
- **Tests (opcional):** casos borde (subtotal 0, zona desconocida) con asserts sobre monto esperado; cotización + checkout con mismo total.

**Criterios de aceptación**

- Total pedido = suma líneas + envío.
- Regla documentada en README o comentario de servicio.

---

## Epic: Experiencia tipo marketplace

### 10. Búsqueda y filtros (Caro In Progress)

**Objetivo**  
Encontrar publicaciones como en un buscador real.

**Qué hacer**

- Parámetros query: `q` (texto en título/descripción), `categoriaId`, `precioMin`, `precioMax`, `orden` (precio asc/desc, fecha).
- Implementar en repositorio (JPQL, Specifications, o QueryDSL).

**Implementación sugerida (búsqueda / capas)**

- **Persistencia:** `ProductoRepository extends JpaRepository<..., Long>, JpaSpecificationExecutor<Producto>` (o `@Query` dinámico con parámetros opcionales); método de servicio `buscar(BusquedaProductoCriteria c, Pageable p)` que compone `Specification` solo con filtros no nulos (`q` con `LIKE` sobre título/descripción, rango de precio, categoría, orden).
- **Contrato API:** mismos query params en `GET /api/products` (tarea 1) o `GET /api/products/search` — documentar contrato; respuesta `Page<ProductResponseDTO>` o lista + metadatos (`totalElements`, `totalPages`).
- **Lógica:** validar `precioMin <= precioMax` si ambos vienen; normalizar `orden` a campos `Sort` seguros (whitelist) para evitar inyección por nombre de columna.
- **Seguridad:** GET público (alineado al catálogo).
- **Errores:** parámetros inválidos → **400**; sin resultados → **200** + página vacía.
- **Tests (opcional):** tests de repositorio con `@DataJpaTest` combinando `q` + rango de precio + paginación.

**Criterios de aceptación**

- Combinar filtros sin romper paginación.
- Si no hay resultados, lista vacía **200**, no 500.

---

### 11. Imágenes del producto

**Objetivo**  
Las publicaciones se ven “reales”.

**Qué hacer (elegir una)**

- **Simple:** lista de URLs en el producto (strings); el front o el admin pega links.
- **Medio:** upload a carpeta local o S3 y guardar la URL en BD.
- **Modelo JPA (alineado a tarea 1):** `@ElementCollection` de URLs → tabla dedicada (p. ej. `product_images` / `producto_imagenes`) en lugar de solo un varchar largo.

**Implementación sugerida (imágenes / capas)**

- **Modelo:** lista ordenada (`@OrderColumn` o `orden` en entidad hija si usan `@OneToMany` explícito); límites máximos en constantes (cantidad, tamaño archivo).
- **Opción URLs:** mutación vía `ProductRequestDTO` / `PUT` producto (tarea 1) o sub-recurso dedicado.
- **Opción upload:** `ProductImageController` — `POST /api/productos/{id}/imagenes` con `MultipartFile[]`; guardar en disco/S3, persistir URL en colección o tabla; validar `Content-Type` y tamaño.
- **Lógica:** `ProductImageService` — solo vendedor dueño o `ROLE_ADMIN`; reordenar opcional `PATCH` con lista de ids/órdenes.
- **Seguridad:** GET detalle producto puede seguir público; POST/DELETE imágenes alineado a mutaciones de producto.
- **Errores:** `ResourceNotFoundException` (producto), **403** si no es vendedor, `BusinessRuleException` si supera máximo de imágenes.
- **Tests (opcional):** upload con MIME inválido → **400**; verificar orden en `GET` detalle.

**Criterios de aceptación**

- GET detalle de producto devuelve imágenes ordenadas.
- Validar cantidad máxima y formato si suben archivos.

---

### 12. Preguntas y respuestas en la publicación

**Objetivo**  
Comprador consulta antes de comprar; vendedor responde (como en ML).

**Qué hacer**

- Entidades `Pregunta` (producto, autor pregunta, texto, fecha), `Respuesta` (opcional 1:1 con pregunta).
- POST pregunta: usuario autenticado, no el vendedor del producto (o permitir ambos según regla de negocio).
- POST respuesta: solo el vendedor del producto.

**Implementación sugerida (Q&A / capas)**

- **Modelo:** `Pregunta` (FK `producto`, FK `autor`, texto, `instanteCreacion`); `Respuesta` (FK `pregunta`, texto, FK `autor` vendedor, 1:1 opcional).
- **Persistencia:** `PreguntaRepository` — `Page<Pregunta> findByProductoIdOrderByInstanteCreacionDesc`; `existsByProductoIdAndAutorId` si evitan autopreguntas.
- **Contrato API:** `PreguntaRequestDTO`, `PreguntaResponseDTO` (incluir `RespuestaResponseDTO` anidada si existe).
- **Mapeo:** `PreguntaMapper`, `RespuestaMapper`.
- **Lógica:** `PreguntaService` / `RespuestaService` — validar usuario autenticado; respuesta solo si `SecurityContext` coincide con `producto.vendedor`.
- **HTTP:** `GET /api/products/{productoId}/preguntas`; `POST /api/products/{productoId}/preguntas`; `POST /api/preguntas/{preguntaId}/respuestas` (o variante anidada).
- **Seguridad:** listado GET público o autenticado según política; POST pregunta con JWT; POST respuesta con rol vendedor dueño.
- **Errores:** `Forbidden`, `ResourceNotFoundException`, `BusinessRuleException` para reglas (ej. duplicada, vendedor preguntando a sí mismo si lo prohiben).
- **Tests (opcional):** integración — comprador crea pregunta; vendedor ajeno no puede responder → **403**.

**Criterios de aceptación**

- Listar preguntas públicas por `productoId`.
- Respuestas visibles solo si existen (o estado publicada).

---

### 13. Reseñas / reputación

**Objetivo**  
Confianza después de una compra cerrada.

**Qué hacer**

- Permitir calificación (1–5 estrellas + comentario opcional) solo si el usuario compró ese producto (o ese vendedor) y el pedido está `ENTREGADO`.
- Agregar en perfil vendedor: promedio de estrellas o cantidad de ventas (derivado).

**Implementación sugerida (reseñas / capas)**

- **Modelo:** `Resena` (FK comprador, FK producto o FK `pedidoLinea`/`PedidoItem` para atar a compra concreta, `puntuacion` 1–5, `comentario` opcional, fecha); **unique** `(usuario_id, pedido_item_id)` o equivalente para idempotencia.
- **Persistencia:** `ResenaRepository`; query `@Query("SELECT AVG(r.puntuacion) ...")` o proyección para resumen vendedor; join con líneas de pedido para comprobar elegibilidad.
- **Contrato API:** `ResenaRequestDTO` (validar rango estrellas), `ResenaResponseDTO`; `VendedorResumenDTO` (promedio, cantidad).
- **Mapeo:** `ResenaMapper`.
- **Lógica:** `ResenaService.crear` — verificar pedido `ENTREGADO`, que el usuario sea comprador de esa línea/producto, no exista reseña previa para esa regla.
- **HTTP:** `POST /api/resenas` o `POST /api/pedidos/{pedidoId}/items/{itemId}/resena`; `GET /api/vendedores/{id}/resumen` o incluir stats en perfil público acotado.
- **Seguridad:** crear reseña autenticado; lectura de resumen según lo que quieran exponer públicamente.
- **Errores:** `ConflictException` (ya reseñó), `BusinessRuleException` (pedido no entregado / no compró), `ResourceNotFoundException`.
- **Tests (opcional):** doble `POST` reseña misma línea → **409**/`Conflict`; pedido no `ENTREGADO` → **400**/regla de negocio.

**Criterios de aceptación**

- No reseñar sin compra válida.
- Una reseña por pedido/producto (o por línea), según definan.

---

## Epic: Cuenta y roles

### 14. Perfil de usuario y flujo comprador vs vendedor

**Objetivo**  
Datos editables y permisos claros según rol.

**Qué hacer**

- GET/PATCH perfil del usuario actual (nombre, apellido, etc.; **no** exponer password en responses).
- Documentar qué puede hacer `USER` y `ADMIN` (tabla en README); `USER` compra y vende (publicaciones propias).
- Perfil: `GET /api/usuarios/me` incluye `publicaciones` y `compras` (compras vacías hasta pedidos).

**Implementación sugerida (usuario + JWT — base de cuenta)**

- **Modelo:** entidad de usuario implementando `UserDetails` (email como “username” de Spring, password, `Role`); enum `Role` (`USER`, `ADMIN`) — alinear nombres con `Usuario` / roles del proyecto.
- **Persistencia:** `UsuarioRepository` — `findByEmail`, `findByUsername`, `existsByEmail`, etc.
- **Servicio:** `AuthenticationService` — registro (BCrypt, rol por defecto), login vía `AuthenticationManager`, generación de JWT; opcional `UserService` / `UsuarioService` para lecturas/altas fuera de auth.
- **HTTP:** `AuthenticationController` — en este repo las rutas son **`/api/auth/register`** y **`/api/auth/login`** → token (string). Si el equipo unifica todo bajo `/api/users`, documentar una sola convención.
- **Seguridad — cadena JWT:** `JwtUtil` (crear/validar token, claims, expiración, roles, extraer email); `JwtFilter` (`Authorization: Bearer`, rellenar `SecurityContextHolder`); `SecurityConfig` con `UserDetailsService` desde BD, `PasswordEncoder`, `AuthenticationManager`, rutas públicas register/login, `JwtFilter` antes del filtro usuario/contraseña; CORS si el front es otro origen (p. ej. `localhost:5173`).
- **Errores:** `ConflictException` + handler para email duplicado en registro; `BadCredentialsException` → **401** en login (`GlobalExceptionHandler`).

**Implementación sugerida (perfil / capas)**

- **Contrato API:** `UserProfileResponseDTO` (sin `password` ni campos internos); `UserProfileUpdateRequestDTO` con campos opcionales (`@Size`, `@Email` si permiten cambiar email).
- **Mapeo:** `UsuarioMapper.updateFromDto(dto, entidad)` ignorando nulls o con estrategia clara (PATCH parcial).
- **Lógica:** `UserService.getMe`, `UserService.updateMe` — cargar usuario por email del JWT; si cambian email, `existsByEmail` y `ConflictException`.
- **HTTP:** `GET /api/usuarios/me`, `PATCH /api/usuarios/me` (o `PUT` si prefieren reemplazo completo).
- **Seguridad:** autenticado; nunca `GET/PATCH /api/usuarios/{id}` para terceros salvo admin explícito.
- **Errores:** `ResourceNotFoundException`, `ConflictException` (email duplicado), validación Bean Validation → **400**.
- **Tests (opcional):** `PATCH /me` con JWT; sin token → **401**; email ya usado → **409**.

**Criterios de aceptación**

- No puedo editar el perfil de otro usuario.
- Cambio de email con validación si lo agregan.

---

### 15. Favoritos / guardados 

**Objetivo**  
Guardar publicaciones para después.

**Qué hacer**

- Tabla `Favorito` (usuario + producto) con unique (usuario, producto).
- POST/DELETE y GET listado.

**Implementación sugerida (favoritos / capas)**

- **Modelo:** entidad `Favorite` / `Favorito` (`favorites`): `ManyToOne` a usuario y producto.
- **Persistencia:** `FavoriteRepository` — `findByUserId`, `findByUserIdAndProductId` (duplicados / consultas por usuario).
- **Contrato API:** `FavoriteRequestDTO`, `FavoriteResponseDTO` (validación en POST).
- **Mapeo:** `FavoriteMapper` ↔ entidad.
- **Lógica:** `FavoriteService` — listar por usuario, obtener por id, añadir (validar usuario, producto, duplicado), borrar solo si el favorito es del usuario; apoyarse en servicio/repo de producto y usuario según el diseño.
- **HTTP:** `FavoriteController` `/api/favorites`: `GET /` del autenticado, `GET /{id}`, `GET /user/{userId}` (evaluar restricción por seguridad), `POST /` (body + `Authentication`), `DELETE /{id}` si es del usuario.
- **Seguridad:** rutas autenticadas (no públicas como GET de catálogo ni register/login); resolver usuario con `Authentication` + `UserRepository`.
- **Errores:** `ConflictException`, `BusinessRuleException`, `ResourceNotFoundException` según validaciones en servicio.
- **Tests (opcional):** `FavoriteServiceTest` (duplicado → conflict); integración con JWT y `GET /user/{id}` si lo exponen (solo propio o admin).

**Criterios de aceptación**

- Idempotente: marcar dos veces el mismo favorito no duplica filas.

---

## Definition of Done (sugerido para todas las tareas)

- Código revisado por al menos una persona.
- README o comentarios actualizados si cambia el contrato de la API.
- Casos felices probados (Postman, curl o tests automáticos según acuerdo del equipo).

# Ecommerce API (TPO)

## Roles y permisos (resumen)

| Rol (`Role`) | Descripción breve |
|--------------|---------------------|
| **USER** | Rol por defecto al registrarse. Puede comprar (carrito, etc.) y publicar/gestionar **sus** productos (alta/edición/borrado como dueño). |
| **ADMIN** | Acceso administrativo (p. ej. rutas `/api/admin/**`) y puede modificar productos aunque no sea el vendedor. |

La API no expone contraseñas en las respuestas de perfil (`GET/PATCH /api/usuarios/me`). En `GET /api/usuarios/me` también se devuelven `publicaciones` (resumen de productos donde el usuario es vendedor) y `compras` (lista vacía hasta que exista el módulo de pedidos).

Si tenías filas con el valor de rol antiguo `VENDEDOR` en la base, migrá con: `UPDATE usuarios SET role = 'USER' WHERE role = 'VENDEDOR';` antes de levantar esta versión.

Si cambiás el **email** con `PATCH /api/usuarios/me`, el JWT viejo sigue teniendo el email anterior en el `sub`: tenés que **volver a hacer login** con el email nuevo para obtener un token válido.

## Favoritos (`/api/favorites`)

Todas las rutas requieren JWT (`Authorization: Bearer <token>`).

| Método | Ruta                             | Descripción                                 |
|--------|----------------------------------|---------------------------------------------|
| GET    | `/api/favorites`                 | Lista los favoritos del usuario autenticado  |
| GET    | `/api/favorites/{id}`            | Detalle de un favorito (solo propio)         |
| GET    | `/api/favorites/user/{userId}`   | Favoritos de un usuario (propio o admin)     |
| POST   | `/api/favorites`                 | Agregar producto a favoritos                 |
| DELETE | `/api/favorites/{id}`            | Quitar de favoritos (solo propio)            |

**POST body:**
```json
{ "productId": 1 }

Respuesta POST 
{
  "id": 1,
  "productId": 1,
  "nombreProducto": "Mouse Logitech G203",
  "precio": 45.99
}

Errores posibles:

409 Conflict — El producto ya está en favoritos (no se duplican filas).
404 Not Found — Producto o favorito no existe.
403 Forbidden — Intentar ver/borrar favoritos de otro usuario.
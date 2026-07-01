# ms-pedidos

Microservicio de **carrito, checkout y pedidos** de GGStore. Maneja el ciclo de compra completo: carrito de un usuario, wishlist, confirmación de compra (checkout), historial de pedidos y biblioteca de juegos comprados.

## Responsabilidad

- Carrito de compras por usuario.
- Wishlist (lista de deseos) por usuario.
- Checkout: confirma un pedido, valida contra `ms-catalogo` y descuenta stock.
- Historial de pedidos.
- Biblioteca: juegos que el usuario ya compró.
- Estadísticas administrativas (para el dashboard de `ms-bff`).

Este servicio **no maneja autenticación**: confía en el header `X-Usuario-Id`, que le llega ya validado desde `ms-bff` (que verificó el JWT antes de reenviar la petición).

## Stack técnico

| Componente | Detalle |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot (Web MVC) |
| Persistencia | Spring Data JPA + PostgreSQL |
| Validación | Spring Validation |
| Cliente HTTP | `RestClient` (Spring 6.1+), para llamar a `ms-catalogo` |
| Utilidades | Lombok |
| Tests | JUnit 5, Mockito, AssertJ, H2 (in-memory) |
| Cobertura | JaCoCo |

## Estructura del proyecto

```
src/main/java/com/ggstore/ms_pedidos/
├── controller/     # Carrito, Checkout, Biblioteca, Wishlist, Admin
├── service/        # Lógica de negocio
├── repository/     # Interfaces Spring Data JPA
├── model/          # Entidades JPA (Carrito, Pedido, Biblioteca, Cupon, Wishlist...)
├── dto/            # Objetos de transferencia
├── client/         # JuegoClient: llamadas HTTP a ms-catalogo
├── config/         # Configuración de beans (RestClient, etc.)
└── exception/      # GlobalExceptionHandler
```

## Cómo levantarlo local

### Requisitos
- JDK 17
- Maven (o el wrapper `mvnw` / `mvnw.cmd` incluido)
- Acceso a una base PostgreSQL
- `ms-catalogo` corriendo (para las llamadas de precio/stock)

### Variables de entorno / configuración

```properties
spring.application.name=ms-pedidos

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
server.port=8082

ms-catalogo.url=http://localhost:8081
```

> ⚠️ Igual que en `ms-catalogo`, el `application.properties` del repo tiene credenciales de base de datos hardcodeadas. Recomendado migrarlas a variables de entorno antes de compartir el repo más ampliamente.

### Levantar el servicio

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8082`. Necesita que `ms-catalogo` esté corriendo en `http://localhost:8081` (o la URL que se configure en `ms-catalogo.url`).

## Endpoints principales

Todos los endpoints (salvo `/admin/stats`) esperan el header `X-Usuario-Id: <uuid>`, que en producción lo agrega `ms-bff` a partir del JWT.

### Carrito — `/carrito`
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/carrito` | Obtiene el carrito del usuario |
| POST | `/carrito/items` | Agrega un juego al carrito |
| PUT | `/carrito/items/{detalleId}?cantidad=` | Actualiza cantidad de un ítem |
| DELETE | `/carrito/items/{detalleId}` | Elimina un ítem del carrito |

### Checkout / Pedidos — `/pedidos`
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/pedidos/checkout` | Confirma la compra del carrito actual (body opcional con cupón) |
| GET | `/pedidos/historial` | Historial de pedidos del usuario |

### Biblioteca — `/biblioteca`
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/biblioteca` | Juegos que el usuario ya compró |

### Wishlist — `/wishlist`
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/wishlist` | Lista de deseos del usuario |
| POST | `/wishlist/{juegoId}` | Agrega un juego a la wishlist |
| DELETE | `/wishlist/{id}` | Elimina un ítem de la wishlist |

### Admin — `/admin`
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/admin/stats` | Estadísticas de ventas (consumido por el dashboard de `ms-bff`) |

## Tests y cobertura

```bash
./mvnw test
```

El `JuegoClient` (llamadas salientes a `ms-catalogo`) se testea mockeando el `RestClient`; el resto sigue el mismo patrón de Mockito puro sobre `service` y `controller`.

Reporte de cobertura:

```
target/site/jacoco/index.html
```

## Servicios relacionados

- Depende de `ms-catalogo` para consultar precio/stock de juegos (vía `JuegoClient`) y para descontar stock al confirmar un checkout.
- `ms-bff` reenvía (proxy) las peticiones de `/carrito`, `/pedidos`, `/biblioteca` y `/wishlist` hacia este servicio, agregando el header `X-Usuario-Id` a partir del JWT del usuario autenticado.

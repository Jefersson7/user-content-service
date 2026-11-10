# user-content-service

Microservicio que ingesta usuarios, posts y comentarios desde [JSONPlaceholder](https://jsonplaceholder.typicode.com), los persiste en una base de datos relacional y expone endpoints de consulta agregados.

## Tecnologías

- Java 25
- Spring Boot 4.1.2
- Spring Data JPA
- H2 (modo archivo)
- Caffeine (caché)
- Resilience4j (CircuitBreaker y timeout)
- JUnit 5 y Mockito
- Maven

## Rutas de la API

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/sync/{userId}` | Consulta el usuario, sus posts y comentarios en JSONPlaceholder y los persiste |
| GET | `/api/users/{id}/summary` | Devuelve el usuario con sus posts y comentarios agregados |
| GET | `/api/posts/top?limit=3` | Devuelve los posts con los ids más altos |

### Ejemplos

```bash
curl -X POST http://localhost:8080/api/sync/1
# {"userId":1,"posts":10,"comments":50}

curl http://localhost:8080/api/users/1/summary
# {"userId":1,"userName":"Bret","totalPost":10,"totalComments":50,"posts":[...]}

curl 'http://localhost:8080/api/posts/top?limit=3'
# [{"id":100,"userId":10,"title":"...","body":"..."}, ...]
```

Los errores usan `application/problem+json` (RFC 9457):

```json
{
  "detail": "User 999 not found",
  "instance": "/api/users/999/summary",
  "status": 404,
  "title": "Not Found"
}
```

## Ejecución local

Requisitos: JDK 25.

```bash
./mvnw spring-boot:run
```

El perfil `local` está activo por defecto. La aplicación queda disponible en `http://localhost:8080`.

Si Maven no está usando JDK 25, apunta `JAVA_HOME` a esa versión.

### Base de datos

| Elemento | Valor |
|---|---|
| Motor | H2 en modo archivo |
| Archivo | `./data/usercontent.mv.db` |
| Usuario | `sa` |
| Password | vacío |

### Consola H2

```
http://localhost:8080/h2-console
```

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/usercontent;MODE=PostgreSQL;AUTO_SERVER=TRUE` |
| User Name | `sa` |
| Password | vacío |

La consola corre en el mismo JVM que la aplicación, así que no hay conflicto de lock.

### Cliente de base de datos

Con la aplicación en ejecución, cualquier cliente compatible con H2 (DataGrip, DBeaver, IntelliJ, etc.) puede conectarse:

| Campo | Valor |
|---|---|
| Driver | H2 2.4.240 |
| URL | `jdbc:h2:file:<RUTA_DEL_PROYECTO>/data/usercontent;MODE=PostgreSQL;AUTO_SERVER=TRUE` |
| Usuario | `sa` |
| Password | vacío |

Reemplaza `<RUTA_DEL_PROYECTO>` por la ruta absoluta donde clonaste el repositorio. `AUTO_SERVER=TRUE` permite que el cliente y la aplicación usen la misma base al mismo tiempo.

Si la aplicación está detenida, la misma URL sigue funcionando.

## Arquitectura

Arquitectura hexagonal (puertos y adaptadores):

```
domain/
├── model/          User, Post, Comment, UserAggregate
├── port/in/        SyncUserUseCase, GetUserSummaryUseCase, GetTopPostsUseCase
├── port/out/       ExternalContentPort, UserRepositoryPort, PostQueryPort
└── exception/      UserNotFoundException, ExternalApiException

application/        SyncUserService, GetUserSummaryService, GetTopPostsService

infrastructure/
├── config/         AppProperties, InfrastructureConfig
├── adapter/in/rest/ SyncController, SummaryController, TopPostsController, ApiExceptionHandler
├── adapter/out/client/ JsonPlaceholderClient
└── adapter/out/persistence/ entidades, repositorios, adaptadores y mapper
```

El paquete `domain` no importa Spring, JPA ni Jackson. Las dependencias apuntan hacia adentro.

## Configuración

| Propiedad | Valor por defecto | Propósito |
|---|---|---|
| `app.external.base-url` | `https://jsonplaceholder.typicode.com` | Proveedor externo |
| `app.external.connect-timeout` | `2s` | Timeout de conexión HTTP |
| `app.external.read-timeout` | `3s` | Timeout de lectura HTTP |
| `app.cache.ttl` | `5m` | TTL de las entradas de caché |
| `resilience4j.timelimiter.instances.jsonplaceholder.timeout-duration` | `3s` | Timeout del circuit breaker |

## Pruebas

```bash
./mvnw verify
```

11 pruebas unitarias cubren los casos de uso, el cliente externo y el contexto de la aplicación.

## Cobertura de requisitos

| Requisito | Implementación |
|---|---|
| Consumo de API externa | `JsonPlaceholderClient` para `/users`, `/posts` y `/comments` |
| Transformación de datos | Records de dominio, `PersistenceMapper` y mappers de DTO REST |
| Persistencia | H2 en modo archivo, `schema.sql`, entidades y repositorios JPA |
| Endpoints REST | Controllers de sync, summary y top posts |
| Arquitectura hexagonal | Puertos en `domain.port`, adaptadores en `infrastructure` |
| SOLID | Puertos pequeños, clases con una responsabilidad y dependencias hacia adentro |
| CircuitBreaker | Resilience4j alrededor del cliente externo |
| Timeout | Timeouts del cliente HTTP y TimeLimiter de Resilience4j |
| Caché con TTL configurable | Caffeine con `app.cache.ttl` y evicción al sincronizar |
| Logs | Inicio, fin y fallo del sync con id de usuario, conteos y duración |
| Pruebas unitarias | 11 pruebas con JUnit 5 y Mockito |

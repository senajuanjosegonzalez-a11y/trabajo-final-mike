# Carvajal Wishlist — Arquitectura de Microservicios

Prueba técnica **Desarrollador Expert**: módulo de **Lista de Deseos**
para el catálogo B2C de Carvajal, construido como **arquitectura de
microservicios** en **Java 17 + Spring Boot**, con **autenticación JWT**,
persistencia en **MySQL** vía **Spring Data JPA**, **contenerización con
Docker** y un **frontend en Angular**.

> Este proyecto parte de la base entregada por el profesor (`jwtMike`) y
> de una primera versión monolítica (`jwt-wishlist-carvajal`), que aquí se
> separó en 3 microservicios independientes + frontend, cada uno con su
> propia base de datos, siguiendo el patrón **Database per Service**.

---

## 1. Arquitectura general

```
                         ┌───────────────────────┐
                         │   Frontend (Angular)    │
                         │   http://localhost:4200 │
                         └───────────┬─────────────┘
                                     │ HTTP + Bearer JWT
              ┌──────────────────────┼───────────────────────┐
              │                      │                        │
   ┌──────────▼─────────┐ ┌──────────▼──────────┐  ┌──────────▼───────────┐
   │   auth-service       │ │   product-service     │  │   wishlist-service     │
   │   :8081               │ │   :8082                │  │   :8083                 │
   │   register/login/     │ │   catálogo de          │  │   listar/agregar/        │
   │   refreshToken         │ │   productos + stock     │  │   actualizar/eliminar     │
   │   emite el JWT          │ │   valida JWT (solo      │  │   deseos + histórico        │
   │                          │ │   lectura)               │  │   valida JWT y CONSULTA     │
   │                          │ │                          │  │   product-service por REST   │
   └──────────┬─────────┘ └──────────┬──────────┘  └──────────┬───────────┘
              │                      │                        │
              ▼                      ▼                        ▼
         ┌─────────┐            ┌───────────┐            ┌────────────┐
         │ auth_db  │            │ product_db │            │ wishlist_db │
         └─────────┘            └───────────┘            └────────────┘
                    (una sola instancia de MySQL, 3 bases de datos)
```

### ¿Por qué así?

- **auth-service** es el único que conoce contraseñas y emite el JWT.
- **product-service** y **wishlist-service** **validan el JWT ellos
  mismos** (filtro propio, igual al `JwtValidationFilter` del proyecto
  base), usando la **misma llave secreta compartida** (`JWT_SECRET_KEY`).
  Así cada servicio es autónomo: no necesita llamar a `auth-service` en
  cada petición para saber si el token es válido.
- **wishlist-service NO tiene acceso a la base de datos de productos.**
  Cuando necesita saber si un producto existe o cuánto stock tiene, hace
  una petición HTTP real a `product-service` (clase `ProductClient`,
  usando `RestTemplate`). Esa es la comunicación síncrona entre
  microservicios que pide la prueba.
- No se incluyó un API Gateway (Spring Cloud Gateway) ni Service Discovery
  (Eureka) para mantener el alcance controlado dentro del tiempo de la
  prueba: el frontend llama directamente a cada microservicio por su
  puerto. La sección **"Posibles mejoras"** explica cómo se agregaría.

---

## 2. Tecnologías utilizadas

| Capa                     | Tecnología                                                        |
|---------------------------|---------------------------------------------------------------------|
| Backend                   | Java 17, Spring Boot 4.1 (`spring-boot-starter-webmvc`)              |
| Persistencia / ORM        | Spring Data JPA (Hibernate)                                          |
| Base de datos              | MySQL 8                                                              |
| Seguridad                  | JWT (`jjwt` 0.12.7), `BCryptPasswordEncoder`, filtro propio (`OncePerRequestFilter`) |
| Comunicación entre servicios | REST síncrono con `RestTemplate`                                    |
| Frontend                    | Angular 18 (standalone components, signals, `HttpClient` con interceptor) |
| Contenerización             | Docker + Docker Compose (multi-stage builds)                        |
| Build backend                | Maven (`mvnw` incluido)                                             |
| Build frontend                | npm / Angular CLI                                                   |

---

## 3. Estructura del repositorio

```
wishlist-microservices/
├── auth-service/         → microservicio de autenticación (Spring Boot)
├── product-service/      → microservicio de catálogo de productos
├── wishlist-service/     → microservicio de lista de deseos
├── frontend/              → aplicación Angular
├── docs/
│   ├── MODELO-BASE-DATOS.md   → modelo de datos de las 3 bases
│   └── init-db/                 → script que crea las 3 bases en MySQL
├── docker-compose.yml     → levanta TODO el stack con un solo comando
└── README.md               → este archivo
```

Cada microservicio replica la misma arquitectura en capas que ya traía el
proyecto base: `controller → service → repository → entity/dto`, más
`filter`/`config` para JWT y CORS.

---

## 4. Cómo desplegar y probar (opción recomendada: Docker Compose)

### Requisitos previos

- Docker y Docker Compose instalados (Docker Desktop en Windows/Mac, o
  `docker` + `docker compose` en Linux).
- Nada más: **no necesitas tener Java, Maven, Node ni MySQL instalados en
  tu máquina**, todo corre dentro de los contenedores.

### Pasos

```bash
cd wishlist-microservices
cp .env.example .env
openssl rand -base64 32
# Copia el resultado anterior en JWT_SECRET_KEY dentro de .env.
docker compose up --build
```

Esto levanta, en orden:

1. **MySQL** (crea automáticamente `auth_db`, `product_db`, `wishlist_db`
   gracias a `docs/init-db/01-create-databases.sql`).
2. **auth-service**, **product-service**, **wishlist-service** (cada uno
   crea sus propias tablas y carga sus datos simulados al arrancar, vía
   `schema.sql` / `data.sql`).
3. **frontend** (Angular compilado y servido con Nginx).

Cuando todos los contenedores digan que están arriba:

| Servicio          | URL                                    |
|---------------------|------------------------------------------|
| Frontend (Angular)   | http://localhost:4200                    |
| auth-service          | http://localhost:8081/api/v1              |
| product-service       | http://localhost:8082/api/v1              |
| wishlist-service      | http://localhost:8083/api/v1              |
| MySQL                  | localhost:3306 (user `root`, password `root`) |

Para apagar todo:

```bash
docker compose down          # detiene y elimina los contenedores
docker compose down -v       # además borra el volumen de MySQL (datos)
```

### Probar la aplicación desde el navegador

1. Abrir **http://localhost:4200**.
2. Clic en **"Crear cuenta"**, registrarte como `CLIENTE`.
3. Iniciar sesión.
4. En **Catálogo**, agregar uno o varios productos a la lista de deseos
   (fíjate en los que dicen **"Sin stock"**: agrégalos igual para luego
   ver cómo se marca en la lista de deseos... en realidad el botón se
   deshabilita si el stock es 0, prueba mejor con uno que sí tenga stock
   y luego bájale el stock manualmente en la base de datos para ver la
   notificación, ver sección 7).
5. En **Mi lista de deseos**: cambiar cantidades (+/-), eliminar
   productos, ver el total estimado.

---

## 5. Cómo desplegar en modo desarrollo (sin Docker, servicio por servicio)

Útil si quieres depurar un microservicio puntual desde tu IDE.

### Requisitos previos

- JDK 17
- Maven (o el wrapper `./mvnw` incluido en cada servicio)
- Node.js 18+ y npm
- MySQL 8 corriendo localmente

### 5.1. Base de datos

```sql
CREATE DATABASE auth_db;
CREATE DATABASE product_db;
CREATE DATABASE wishlist_db;
```

(Las tablas y los datos simulados los crea cada microservicio solo, al
arrancar, gracias a `schema.sql` + `data.sql` de cada uno.)

### 5.2. Variables de entorno

Cada servicio lee sus variables con valores por defecto ya pensados para
correr en `localhost` (ver `application.yaml` de cada uno), así que
**puedes correrlos sin configurar nada** si tu MySQL local usa
`root`/`root`. Si tu MySQL tiene otra contraseña, exporta antes de
arrancar:

```bash
export DB_USER=root
export DB_PASSWORD=tu_password
export JWT_SECRET_KEY=$(openssl rand -base64 32)   # debe ser LA MISMA en los 3 servicios
```

> **Importante:** `JWT_SECRET_KEY` debe ser idéntica en `auth-service`,
> `product-service` y `wishlist-service`, porque los dos últimos validan
> el token firmado por el primero usando esa misma llave. Si no la
> exportas, los 3 servicios usan el mismo valor por defecto ya incluido en
> cada `application.yaml` (solo para efectos de prueba/demo).

### 5.3. Levantar cada microservicio (en 3 terminales distintas)

```bash
cd auth-service      && ./mvnw spring-boot:run     # puerto 8081
cd product-service   && ./mvnw spring-boot:run     # puerto 8082
cd wishlist-service  && ./mvnw spring-boot:run     # puerto 8083
```

### 5.4. Levantar el frontend

```bash
cd frontend
npm install
npm start              # equivale a: ng serve, en http://localhost:4200
```

El frontend ya está configurado (`src/environments/environment.ts`) para
apuntar a `localhost:8081/8082/8083`, así que funciona igual en local que
en Docker.

---

## 6. Credenciales y datos precargados

- **Base de datos:** las credenciales se leen desde `.env`, que no debe
  versionarse.
- **Usuarios de la aplicación:** el proyecto **no trae usuarios
  precreados**. Se crean desde el frontend (**Crear cuenta**) o llamando
  directamente `POST /api/v1/auth/register` en `auth-service`. Los roles
  disponibles ya están precargados: `CLIENTE` (id 1) y `ADMIN` (id 2).
- **Catálogo de productos:** se simula automáticamente vía `data.sql` en
  `product-service` (8 productos, algunos con `stock = 0` a propósito
  para poder demostrar el requisito de "notificar si ya no hay stock").
- **JWT:** la llave secreta y el tiempo de expiración se configuran mediante
  (`JWT_SECRET_KEY`, `JWT_EXPIRATION`); el ejemplo expira en 1 hora
  (`3600000` ms).

---

## 7. Endpoints principales (por microservicio)

### auth-service — `http://localhost:8081/api/v1`

| Método | Endpoint              | Body                                            | Descripción              |
|--------|-------------------------|--------------------------------------------------|-----------------------------|
| POST   | `/auth/register`         | `{ username, email, password, rolId }`             | Registra un usuario          |
| POST   | `/auth/login`             | `{ user, password }` (user = username o email)      | Retorna el JWT                |
| POST   | `/auth/refreshToken`      | header `Authorization: Bearer <token>`                | Renueva el JWT                 |

### product-service — `http://localhost:8082/api/v1` (requiere Bearer token)

| Método | Endpoint          | Descripción                                 |
|--------|---------------------|------------------------------------------------|
| GET    | `/products`           | Lista el catálogo con cantidades en stock        |
| GET    | `/products/{id}`      | Consulta un producto puntual (también la usa wishlist-service internamente) |

### wishlist-service — `http://localhost:8083/api/v1` (requiere Bearer token)

| Método | Endpoint               | Body                                | Descripción                                                         |
|--------|--------------------------|----------------------------------------|-------------------------------------------------------------------------|
| GET    | `/wishlist`               | —                                       | Lista la wishlist del usuario autenticado, notificando si algo no tiene stock |
| POST   | `/wishlist`                | `{ productId, quantity }`                | Agrega un producto                                                       |
| PUT    | `/wishlist/{id}`           | `{ quantity }`                          | Actualiza la cantidad                                                    |
| DELETE | `/wishlist/{id}`            | —                                       | Elimina un producto de la lista                                          |

Cada `POST`, `PUT` y `DELETE` sobre `/wishlist` genera automáticamente un
registro en `wishlist_history` (tabla de histórico, requisito obligatorio
de la prueba), independientemente de si el ítem sigue vigente o no.

### Ejemplo rápido con `curl`

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@test.com","password":"12345678","rolId":1}'

# 2. Login (guarda el jwt de la respuesta)
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"user":"ana","password":"12345678"}'

# 3. Ver catálogo
curl http://localhost:8082/api/v1/products -H "Authorization: Bearer <JWT>"

# 4. Agregar producto id=1 a la wishlist
curl -X POST http://localhost:8083/api/v1/wishlist \
  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 5. Simular que el producto se agotó (para ver la notificación):
#    conéctate a product_db y baja el stock a 0:
#    UPDATE products SET stock = 0 WHERE id = 1;
#    Luego vuelve a consultar la wishlist:
curl http://localhost:8083/api/v1/wishlist -H "Authorization: Bearer <JWT>"
# La respuesta mostrará "disponible": false y el mensaje de "sin stock"
```

---

## 8. GitFlow (estrategia de ramificación) — plus solicitado

Para trabajar este proyecto con GitFlow:

```bash
git init
git checkout -b develop            # rama de integración
git checkout -b feature/auth-service develop
# ... commits del microservicio de autenticación ...
git checkout develop && git merge --no-ff feature/auth-service

git checkout -b feature/product-service develop
# ... commits del microservicio de catálogo ...
git checkout develop && git merge --no-ff feature/product-service

git checkout -b feature/wishlist-service develop
# ... commits del microservicio de wishlist ...
git checkout develop && git merge --no-ff feature/wishlist-service

git checkout -b feature/frontend-angular develop
# ... commits del frontend ...
git checkout develop && git merge --no-ff feature/frontend-angular

# Cuando develop está estable y listo para entrega:
git checkout -b release/1.0.0 develop
# ajustes finales, README, docker-compose...
git checkout main && git merge --no-ff release/1.0.0 && git tag v1.0.0
git checkout develop && git merge --no-ff release/1.0.0
```

Ramas usadas: `main` (versión estable/entregable), `develop` (integración),
`feature/*` (una por microservicio o módulo), `release/*` (estabilización
antes de entrega). Si aparece un bug en producción, se usaría además
`hotfix/*` partiendo de `main`.

---

## 9. Posibles mejoras (no implementadas, para tenerlas presentes en la sustentación)

- **API Gateway** (Spring Cloud Gateway): un único punto de entrada
  (`:8080`) que enrute `/auth/**`, `/products/**`, `/wishlist/**` a cada
  microservicio, y opcionalmente centralice la validación del JWT ahí en
  vez de repetirla en cada servicio.
- **Service Discovery** (Eureka / Consul): para que los servicios se
  encuentren entre sí por nombre lógico en vez de URLs fijas por variable
  de entorno — útil cuando hay múltiples instancias de un mismo servicio.
- **Comunicación asíncrona** (RabbitMQ/Kafka) para eventos como
  "producto agotado" en vez de que wishlist-service consulte a
  product-service en cada lectura.
- **Circuit breaker** (Resilience4j) en `ProductClient` para tolerar caídas
  de product-service sin tumbar wishlist-service.
- **Base de datos por servicio en instancias separadas** (hoy comparten un
  mismo contenedor de MySQL por simplicidad de despliegue local).

---

## 10. Notas para la sustentación

- El **JWT se emite solo en auth-service** y se **valida de forma
  independiente en cada microservicio protegido** (product-service y
  wishlist-service) usando la misma llave secreta — esto demuestra
  entendimiento de autenticación distribuida sin acoplar los servicios
  entre sí en tiempo de ejecución.
- La comunicación entre `wishlist-service` y `product-service` es el punto
  central para explicar "por qué es microservicios y no un monolito con
  paquetes": **cada uno tiene su propia base de datos**, y si
  `product-service` estuviera caído, `wishlist-service` seguiría
  funcionando (mostraría el producto como "no disponible" en vez de
  caerse), lo cual se puede demostrar apagando ese contenedor
  (`docker compose stop product-service`) y consultando la wishlist.
- El histórico (`wishlist_history`) registra **cada movimiento**
  (AGREGADO / ACTUALIZADO / ELIMINADO) con fecha, útil para trazabilidad y
  auditoría — es independiente de si el ítem sigue vigente en
  `wishlist_items`.

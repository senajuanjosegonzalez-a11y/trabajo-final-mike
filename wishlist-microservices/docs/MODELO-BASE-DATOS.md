# Modelo de base de datos — Carvajal Wishlist (microservicios)

## Motor de base de datos: MySQL (relacional)

Se eligió un motor **relacional (MySQL 8)** porque:

- La información es naturalmente tabular y con relaciones claras (un usuario
  tiene un rol, una lista de deseos referencia un producto, un histórico
  referencia un movimiento).
- Se necesitan **restricciones de integridad** (unicidad de `email`,
  `username`, `sku`; un mismo producto no puede repetirse dos veces en la
  lista de deseos de un mismo usuario).
- El volumen y la naturaleza de los datos (catálogo, usuarios, wishlist) no
  requieren la flexibilidad de esquema de una base NoSQL; un esquema fijo
  y validado por el motor reduce errores.
- Es el mismo motor que ya usaba el proyecto base entregado por el
  profesor, lo que mantiene consistencia con lo aprendido en clase.

## Patrón "Database per Service"

Cada microservicio es dueño de su propia base de datos y **nadie más accede
a ella directamente** (ni otro microservicio, ni el frontend). Si un
servicio necesita un dato que vive en otra base, lo pide por HTTP al
microservicio dueño de esa información. Esto es lo que permite que cada
servicio se pueda desplegar, escalar y evolucionar de forma independiente.

En este proyecto, por simplicidad de despliegue local, las 3 bases de
datos conviven en **una sola instancia de MySQL** (ver `docker-compose.yml`
y `docs/init-db/01-create-databases.sql`), pero están completamente
separadas entre sí (ninguna tabla de una base tiene FK hacia otra). En un
entorno productivo real, cada una podría vivir en su propio servidor/RDS.

```
MySQL (una sola instancia, 3 bases de datos separadas)
├── auth_db        → dueña: auth-service
├── product_db     → dueña: product-service
└── wishlist_db    → dueña: wishlist-service
```

---

## 1. `auth_db` (auth-service)

### Tabla `roles`

| Columna | Tipo         | Restricciones          |
|---------|--------------|-------------------------|
| id      | BIGINT       | PK, AUTO_INCREMENT      |
| name    | VARCHAR(50)  | NOT NULL, UNIQUE         |

Datos iniciales (`data.sql`): `CLIENTE` (id=1), `ADMIN` (id=2).

### Tabla `users`

| Columna  | Tipo          | Restricciones                        |
|----------|---------------|----------------------------------------|
| id       | BIGINT        | PK, AUTO_INCREMENT                      |
| username | VARCHAR(100)  | NOT NULL, UNIQUE                        |
| email    | VARCHAR(150)  | NOT NULL, UNIQUE                        |
| password | VARCHAR(255)  | NOT NULL (hash BCrypt)                  |
| rol_id   | BIGINT        | NOT NULL, FK → `roles(id)`              |

```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    CONSTRAINT fk_users_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
);
```

---

## 2. `product_db` (product-service)

### Tabla `products`

| Columna     | Tipo           | Restricciones             |
|-------------|-----------------|-----------------------------|
| id          | BIGINT          | PK, AUTO_INCREMENT           |
| sku         | VARCHAR(50)     | NOT NULL, UNIQUE              |
| name        | VARCHAR(150)    | NOT NULL                      |
| description | VARCHAR(500)    |                                |
| price       | DECIMAL(12,2)   | NOT NULL                      |
| stock       | INT             | NOT NULL, DEFAULT 0            |

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);
```

El catálogo se simula (según lo pedido en la prueba) mediante `data.sql`,
con 8 productos, algunos con `stock = 0` para poder demostrar la
notificación de "sin stock" en la lista de deseos.

---

## 3. `wishlist_db` (wishlist-service)

### Tabla `wishlist_items` (lista vigente)

| Columna    | Tipo      | Restricciones                                   |
|------------|-----------|---------------------------------------------------|
| id         | BIGINT    | PK, AUTO_INCREMENT                                  |
| user_id    | BIGINT    | NOT NULL (id del usuario, viene del JWT)            |
| product_id | BIGINT    | NOT NULL (id del producto en `product-service`)     |
| quantity   | INT       | NOT NULL, DEFAULT 1                                 |
| created_at | DATETIME  | NOT NULL                                            |

Restricción: `UNIQUE (user_id, product_id)` — un producto no puede estar
duplicado en la lista de un mismo usuario.

### Tabla `wishlist_history` (histórico de movimientos — requisito obligatorio)

| Columna      | Tipo          | Restricciones                              |
|--------------|----------------|-----------------------------------------------|
| id           | BIGINT         | PK, AUTO_INCREMENT                              |
| user_id      | BIGINT         | NOT NULL                                        |
| product_id   | BIGINT         |                                                  |
| product_name | VARCHAR(150)   | nombre del producto al momento del movimiento   |
| action       | VARCHAR(20)    | `AGREGADO`, `ACTUALIZADO` o `ELIMINADO`         |
| quantity     | INT            |                                                  |
| event_date   | DATETIME       | NOT NULL                                        |

```sql
CREATE TABLE wishlist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE wishlist_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(150),
    action VARCHAR(20) NOT NULL,
    quantity INT,
    event_date DATETIME NOT NULL
);
```

> **Nota:** `wishlist_items.product_id` y `wishlist_history.product_id` **no
> tienen FK física** hacia `products`, porque esa tabla vive en otra base de
> datos (`product_db`), propiedad de otro microservicio. La validez del
> producto se comprueba en tiempo de ejecución llamando por REST a
> `product-service` (ver `ProductClient.java`). Esto es intencional y es
> justamente la diferencia clave entre un modelo relacional monolítico y
> uno de microservicios con "database per service".

---

## Diagrama general (conceptual)

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────────┐
│    auth_db       │      │    product_db      │      │     wishlist_db       │
│                   │      │                    │      │                        │
│  roles            │      │  products          │      │  wishlist_items         │
│   id (PK)         │      │   id (PK)           │◄──┐  │   id (PK)               │
│   name            │      │   sku               │   │  │   user_id               │
│                   │      │   name              │   │  │   product_id  ──────────┼──► (validado por REST,
│  users            │      │   description       │   └──┼── (referencia lógica)   │     no hay FK física)
│   id (PK)         │      │   price             │      │   quantity              │
│   username        │      │   stock             │      │   created_at            │
│   email           │      │                     │      │                        │
│   password        │      └────────────────────┘      │  wishlist_history        │
│   rol_id (FK)     │                                    │   id (PK)               │
└─────────────────┘                                    │   user_id               │
                                                          │   product_id            │
                                                          │   product_name          │
                                                          │   action                │
                                                          │   quantity              │
                                                          │   event_date            │
                                                          └─────────────────────┘
```

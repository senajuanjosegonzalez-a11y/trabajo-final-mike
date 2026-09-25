# Carvajal Wishlist

Aplicacion B2C de lista de deseos construida con microservicios. Permite crear usuarios, iniciar sesion, consultar un catalogo de productos y agregar, actualizar o eliminar productos de una lista de deseos.

El proyecto esta dentro de [`wishlist-microservices/`](wishlist-microservices/).

## Que incluye

- `auth-service`: registro, login, refresh de JWT y contrasenas con BCrypt.
- `product-service`: catalogo y stock de productos.
- `wishlist-service`: lista de deseos e historico de cambios.
- `frontend`: aplicacion Angular servida por Nginx.
- MySQL 8 con una base de datos por servicio.
- Comunicacion REST entre `wishlist-service` y `product-service`.

## Arquitectura

```text
Angular/Nginx :4200
      |
      +--> auth-service :8081 --> auth_db
      +--> product-service :8082 --> product_db
      +--> wishlist-service :8083 --> wishlist_db
                                      |
                                      +--> product-service por REST

MySQL :3306
```

Los tres servicios Java validan el mismo JWT firmado. El frontend envia el token como `Authorization: Bearer <token>`.

## Requisitos

La forma recomendada necesita solamente:

- Docker Engine o Docker Desktop.
- Docker Compose v2.
- Git.

Para desarrollo sin Docker se necesita Java 17, Node.js 18+, npm y MySQL 8.

## Ejecutar con Docker

Desde la raiz de este repositorio:

```bash
cd wishlist-microservices
cp .env.example .env
sed -i "s|^JWT_SECRET_KEY=.*|JWT_SECRET_KEY=$(openssl rand -base64 32)|" .env
docker compose up --build -d
```

Abrir:

- Frontend: http://localhost:4200
- Auth API: http://localhost:8081/api/v1
- Product API: http://localhost:8082/api/v1
- Wishlist API: http://localhost:8083/api/v1

Para ver el estado y logs:

```bash
docker compose ps
docker compose logs -f auth-service product-service wishlist-service
```

Para detener la aplicacion:

```bash
docker compose down
```

Para borrar tambien los datos locales de MySQL:

```bash
docker compose down -v
```

## Uso rapido

1. Abre el frontend.
2. Selecciona `Crear cuenta` y registra un usuario.
3. Inicia sesion.
4. Entra al catalogo y agrega un producto a deseos.
5. Abre `Mi lista de deseos` para cambiar cantidades o eliminar productos.

## Desarrollo local

Copia las variables de entorno y configura MySQL con las bases `auth_db`, `product_db` y `wishlist_db`:

```bash
cd wishlist-microservices
cp .env.example .env
```

Genera una llave nueva y exporta las variables antes de iniciar los servicios:

```bash
export DB_USER=root
export DB_PASSWORD='tu-password-local'
export JWT_SECRET_KEY="$(openssl rand -base64 32)"
export JWT_EXPIRATION=3600000
```

En terminales separadas:

```bash
cd auth-service && ./mvnw spring-boot:run
cd product-service && ./mvnw spring-boot:run
cd wishlist-service && ./mvnw spring-boot:run
cd frontend && npm ci && npm start
```

## Seguridad

- Nunca subas `.env`, contrasenas, tokens, llaves privadas ni dumps de base de datos.
- Usa una `JWT_SECRET_KEY` aleatoria y diferente por entorno. Debe ser la misma entre los tres servicios del mismo entorno.
- No uses contrasenas de demostracion en produccion.
- Cambia `DB_USER` por un usuario MySQL con permisos minimos en despliegues reales.
- En produccion no expongas MySQL a Internet y coloca las APIs detras de HTTPS, un gateway o un balanceador.
- El archivo [`wishlist-microservices/.env.example`](wishlist-microservices/.env.example) contiene solo placeholders y `.env` esta ignorado por Git.
- Antes de publicar, revisa cambios y secretos con `git diff --check` y una busqueda de patrones sensibles.

El repositorio tenia valores de demo en commits anteriores. Si esa llave o contrasena llegaron a un remoto publico, consideralos comprometidos: rota las credenciales y la llave JWT. Eliminar el secreto del ultimo estado no lo elimina del historial de Git; para limpiar el historial completo se requiere una reescritura coordinada del remoto.

## Estructura

```text
wishlist-microservices/
├── auth-service/
├── product-service/
├── wishlist-service/
├── frontend/
├── docs/
├── .env.example
├── .gitignore
└── docker-compose.yml
```

## Licencia

No se ha definido una licencia para este proyecto.

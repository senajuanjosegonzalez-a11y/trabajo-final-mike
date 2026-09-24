-- Crea una base de datos por microservicio (patrón "database per service").
-- Se ejecuta automáticamente por el contenedor de MySQL al arrancar
-- (montado en /docker-entrypoint-initdb.d).
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS wishlist_db;

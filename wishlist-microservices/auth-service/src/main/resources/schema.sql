-- Script de creación de la base de datos del microservicio de Autenticacion (MySQL)
-- Se ejecuta automáticamente al levantar la aplicación (spring.sql.init.mode=always)

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    CONSTRAINT fk_users_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
);

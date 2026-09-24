-- Script de creación de la base de datos del microservicio de Catalogo de Productos (MySQL)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);

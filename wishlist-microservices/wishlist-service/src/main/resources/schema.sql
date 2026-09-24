-- Script de creación de la base de datos del microservicio de Lista de Deseos (MySQL)

-- Lista de deseos vigente de cada usuario
CREATE TABLE IF NOT EXISTS wishlist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
);

-- Histórico de todos los movimientos que ha tenido la lista de deseos
CREATE TABLE IF NOT EXISTS wishlist_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(150),
    action VARCHAR(20) NOT NULL,
    quantity INT,
    event_date DATETIME NOT NULL
);

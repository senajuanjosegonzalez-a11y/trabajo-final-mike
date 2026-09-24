package com.bananas.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Catalogo de Productos.
 * Expone el catalogo simulado de Carvajal (con cantidades en stock) y es
 * consumido tanto por el frontend como por wishlist-service (para validar
 * existencia/stock de un producto al agregarlo a la lista de deseos).
 */
@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}

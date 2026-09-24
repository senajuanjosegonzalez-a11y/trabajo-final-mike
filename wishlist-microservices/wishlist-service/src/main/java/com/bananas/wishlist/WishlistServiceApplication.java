package com.bananas.wishlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Lista de Deseos.
 * Permite a un cliente autenticado listar, agregar, actualizar y eliminar
 * productos de su lista de deseos, notificando si algún producto ya no
 * tiene stock. Se comunica de forma sincrónica vía REST con
 * product-service para validar existencia/stock, y persiste ademas el
 * histórico completo de movimientos (wishlist_history).
 */
@SpringBootApplication
public class WishlistServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WishlistServiceApplication.class, args);
	}

}

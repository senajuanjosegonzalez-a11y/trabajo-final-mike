package com.bananas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Autenticacion.
 * Responsable de: registrar usuarios, autenticarlos y emitir / refrescar
 * los JSON Web Tokens (JWT) que los demas microservicios (product-service
 * y wishlist-service) validan de forma independiente usando la misma
 * llave secreta compartida (variable de entorno JWT_SECRET_KEY).
 */
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}

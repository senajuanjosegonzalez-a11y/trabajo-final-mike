package com.bananas.wishlist.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bananas.wishlist.dto.ProductDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Cliente REST hacia product-service.
 * Esta es la comunicación sincrónica entre microservicios: wishlist-service
 * NO tiene acceso a la base de datos de productos, siempre pregunta por
 * HTTP. La URL base se inyecta por variable de entorno
 * (PRODUCT_SERVICE_URL), lo que permite apuntar a "localhost" en desarrollo
 * o al nombre del contenedor "product-service" dentro de docker-compose.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    /**
     * Consulta un producto por id en product-service.
     * @return el producto, o null si no existe (404) o el servicio no responde.
     */
    public ProductDTO getProductById(Long productId) {
        try {
            return restTemplate.getForObject(productServiceUrl + "/products/" + productId, ProductDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            log.error("No fue posible contactar a product-service: {}", e.getMessage());
            return null;
        }
    }
}

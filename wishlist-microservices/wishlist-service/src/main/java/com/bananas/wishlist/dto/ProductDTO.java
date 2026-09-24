package com.bananas.wishlist.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Representación del producto tal como lo devuelve product-service.
 * wishlist-service la usa como cliente REST (DTO "espejo"), sin compartir
 * código/entidades entre microservicios.
 */
@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
}

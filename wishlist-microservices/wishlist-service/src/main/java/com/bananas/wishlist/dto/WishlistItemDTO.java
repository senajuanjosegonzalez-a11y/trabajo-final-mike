package com.bananas.wishlist.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private Integer stockDisponible;
    private boolean disponible;
    private String mensaje;
}

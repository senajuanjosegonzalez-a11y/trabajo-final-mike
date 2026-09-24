package com.bananas.wishlist.dto;

import lombok.Data;

@Data
public class WishlistRequestDTO {
    private Long productId;
    private Integer quantity;
}

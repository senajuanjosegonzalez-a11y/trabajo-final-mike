package com.bananas.product.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
}

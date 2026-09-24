package com.bananas.product.dto;

import lombok.Data;

@Data
public class GlobalMessageResponseDTO<T> {
    private String message;
    private T data;
}

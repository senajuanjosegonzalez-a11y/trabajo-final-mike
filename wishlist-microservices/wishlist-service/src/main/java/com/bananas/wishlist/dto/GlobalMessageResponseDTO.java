package com.bananas.wishlist.dto;

import lombok.Data;

@Data
public class GlobalMessageResponseDTO<T> {
    private String message;
    private T data;
}

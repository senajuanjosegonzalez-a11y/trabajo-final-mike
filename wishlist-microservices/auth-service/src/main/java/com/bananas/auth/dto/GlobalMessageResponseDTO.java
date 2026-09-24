package com.bananas.auth.dto;

import lombok.Data;

/**
 * Envoltorio genérico de respuesta usado por todos los microservicios,
 * mantiene el mismo formato { message, data } del proyecto base.
 */
@Data
public class GlobalMessageResponseDTO<T> {
    private String message;
    private T data;
}

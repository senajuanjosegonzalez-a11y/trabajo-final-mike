package com.bananas.product.service;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Valida el JWT emitido por auth-service usando la MISMA llave secreta
 * compartida (variable de entorno JWT_SECRET_KEY). product-service NO
 * emite tokens, solo los valida: cada microservicio es autonomo para
 * proteger sus propios endpoints sin depender de una llamada de red a
 * auth-service en cada peticion.
 */
@Service
public class JwtValidatorService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
    }
}

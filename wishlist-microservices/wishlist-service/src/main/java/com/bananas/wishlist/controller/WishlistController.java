package com.bananas.wishlist.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bananas.wishlist.dto.GlobalMessageResponseDTO;
import com.bananas.wishlist.dto.WishlistItemDTO;
import com.bananas.wishlist.dto.WishlistRequestDTO;
import com.bananas.wishlist.dto.WishlistUpdateRequestDTO;
import com.bananas.wishlist.service.WishlistService;

import lombok.RequiredArgsConstructor;

/**
 * El userId se obtiene del atributo que JwtValidationFilter deja en el
 * request tras validar el token, por lo que todos estos endpoints requieren
 * un Bearer token válido.
 */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<GlobalMessageResponseDTO<List<WishlistItemDTO>>> getWishlist(
            @RequestAttribute("userId") Long userId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(wishlistService.getWishlist(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<GlobalMessageResponseDTO<WishlistItemDTO>> addToWishlist(
            @RequestAttribute("userId") Long userId, @RequestBody WishlistRequestDTO request) {
        try {
            GlobalMessageResponseDTO<WishlistItemDTO> response = wishlistService.addToWishlist(userId, request);
            HttpStatus status = response.getData() != null ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalMessageResponseDTO<WishlistItemDTO>> updateWishlistItem(
            @RequestAttribute("userId") Long userId, @PathVariable Long id,
            @RequestBody WishlistUpdateRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(wishlistService.updateWishlistItem(userId, id, request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalMessageResponseDTO<Void>> removeFromWishlist(
            @RequestAttribute("userId") Long userId, @PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(wishlistService.removeFromWishlist(userId, id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

package com.bananas.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bananas.product.dto.ProductDTO;
import com.bananas.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Lista el catálogo de productos con las cantidades existentes de cada uno */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> listProducts() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(productService.listProducts());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Consulta un producto puntual. Usado por el frontend y, sobre todo,
     * internamente por wishlist-service para validar existencia y stock
     * al agregar/consultar la lista de deseos (comunicación sincrónica
     * entre microservicios vía REST).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }
}

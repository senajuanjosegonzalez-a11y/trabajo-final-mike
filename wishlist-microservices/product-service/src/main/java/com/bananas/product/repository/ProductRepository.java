package com.bananas.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bananas.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

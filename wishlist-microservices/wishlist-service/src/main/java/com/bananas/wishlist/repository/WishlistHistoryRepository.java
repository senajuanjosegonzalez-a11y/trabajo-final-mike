package com.bananas.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bananas.wishlist.entity.WishlistHistory;

public interface WishlistHistoryRepository extends JpaRepository<WishlistHistory, Long> {
}

package com.bananas.wishlist.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Registro histórico de cada movimiento (AGREGADO, ACTUALIZADO, ELIMINADO)
 * que haya ocurrido sobre la lista de deseos de un usuario.
 */
@Entity
@Data
@Table(name = "wishlist_history")
public class WishlistHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "action")
    private String action;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "event_date")
    private LocalDateTime eventDate;
}

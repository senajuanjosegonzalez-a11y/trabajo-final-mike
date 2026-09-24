package com.bananas.wishlist.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bananas.wishlist.client.ProductClient;
import com.bananas.wishlist.dto.GlobalMessageResponseDTO;
import com.bananas.wishlist.dto.ProductDTO;
import com.bananas.wishlist.dto.WishlistItemDTO;
import com.bananas.wishlist.dto.WishlistRequestDTO;
import com.bananas.wishlist.dto.WishlistUpdateRequestDTO;
import com.bananas.wishlist.entity.WishlistHistory;
import com.bananas.wishlist.entity.WishlistItem;
import com.bananas.wishlist.repository.WishlistHistoryRepository;
import com.bananas.wishlist.repository.WishlistItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class WishlistService {

    private static final String ACCION_AGREGADO = "AGREGADO";
    private static final String ACCION_ACTUALIZADO = "ACTUALIZADO";
    private static final String ACCION_ELIMINADO = "ELIMINADO";

    private final WishlistItemRepository wishlistItemRepository;
    private final WishlistHistoryRepository wishlistHistoryRepository;
    private final ProductClient productClient;

    /**
     * Lista la lista de deseos del usuario autenticado, notificando si algún
     * producto ya no cuenta con stock suficiente (o ya no existe en el
     * catálogo). Por cada item se consulta product-service vía REST.
     */
    public GlobalMessageResponseDTO<List<WishlistItemDTO>> getWishlist(Long userId) {
        GlobalMessageResponseDTO<List<WishlistItemDTO>> response = new GlobalMessageResponseDTO<>();

        List<WishlistItemDTO> items = wishlistItemRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        response.setData(items);
        response.setMessage("Lista de deseos obtenida correctamente");
        return response;
    }

    public GlobalMessageResponseDTO<WishlistItemDTO> addToWishlist(Long userId, WishlistRequestDTO request) {
        GlobalMessageResponseDTO<WishlistItemDTO> response = new GlobalMessageResponseDTO<>();

        ProductDTO product = productClient.getProductById(request.getProductId());
        if (product == null) {
            response.setMessage("El producto no existe en el catálogo");
            return response;
        }

        if (wishlistItemRepository.findByUserIdAndProductId(userId, request.getProductId()).isPresent()) {
            response.setMessage("El producto ya se encuentra en la lista de deseos");
            return response;
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(product.getId());
        item.setQuantity(request.getQuantity() == null ? 1 : request.getQuantity());
        item.setCreatedAt(LocalDateTime.now());

        wishlistItemRepository.save(item);

        registrarHistorico(userId, product.getId(), product.getName(), ACCION_AGREGADO, item.getQuantity());

        response.setData(toDTO(item));
        response.setMessage("Producto agregado a la lista de deseos");
        return response;
    }

    public GlobalMessageResponseDTO<WishlistItemDTO> updateWishlistItem(Long userId, Long id,
            WishlistUpdateRequestDTO request) {
        GlobalMessageResponseDTO<WishlistItemDTO> response = new GlobalMessageResponseDTO<>();

        Optional<WishlistItem> itemOpt = wishlistItemRepository.findByIdAndUserId(id, userId);
        if (itemOpt.isEmpty()) {
            response.setMessage("El producto no se encuentra en la lista de deseos");
            return response;
        }

        WishlistItem item = itemOpt.get();
        item.setQuantity(request.getQuantity());
        wishlistItemRepository.save(item);

        ProductDTO product = productClient.getProductById(item.getProductId());
        String productName = product != null ? product.getName() : null;

        registrarHistorico(userId, item.getProductId(), productName, ACCION_ACTUALIZADO, item.getQuantity());

        response.setData(toDTO(item));
        response.setMessage("Producto de la lista de deseos actualizado correctamente");
        return response;
    }

    public GlobalMessageResponseDTO<Void> removeFromWishlist(Long userId, Long id) {
        GlobalMessageResponseDTO<Void> response = new GlobalMessageResponseDTO<>();

        Optional<WishlistItem> itemOpt = wishlistItemRepository.findByIdAndUserId(id, userId);
        if (itemOpt.isEmpty()) {
            response.setMessage("El producto no se encuentra en la lista de deseos");
            return response;
        }

        WishlistItem item = itemOpt.get();
        ProductDTO product = productClient.getProductById(item.getProductId());
        String productName = product != null ? product.getName() : null;

        wishlistItemRepository.delete(item);

        registrarHistorico(userId, item.getProductId(), productName, ACCION_ELIMINADO, item.getQuantity());

        response.setMessage("Producto eliminado de la lista de deseos");
        return response;
    }

    private void registrarHistorico(Long userId, Long productId, String productName, String action,
            Integer quantity) {
        WishlistHistory history = new WishlistHistory();
        history.setUserId(userId);
        history.setProductId(productId);
        history.setProductName(productName);
        history.setAction(action);
        history.setQuantity(quantity);
        history.setEventDate(LocalDateTime.now());

        wishlistHistoryRepository.save(history);
    }

    private WishlistItemDTO toDTO(WishlistItem item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());

        ProductDTO product = productClient.getProductById(item.getProductId());

        if (product != null) {
            dto.setProductName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setStockDisponible(product.getStock());

            boolean disponible = product.getStock() != null && product.getStock() >= item.getQuantity();
            dto.setDisponible(disponible);
            dto.setMensaje(disponible ? null : "Este producto ya no cuenta con stock suficiente");
        } else {
            dto.setDisponible(false);
            dto.setMensaje("Este producto ya no existe en el catálogo");
        }

        return dto;
    }
}

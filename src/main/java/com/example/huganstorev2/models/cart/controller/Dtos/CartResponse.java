package com.example.huganstorev2.models.cart.controller.Dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.huganstorev2.models.cart.entity.Cart;
import com.example.huganstorev2.models.cart.entity.CartItem;

import lombok.Data;

@Data 
public class CartResponse {
    private Long cartId;
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalPrice;

    public CartResponse(Cart cart){
        this.cartId = cart.getId();
        this.userId = cart.getUser().getId();

        // Chuyển từ CartItem -> CartItemResponse
        this.items = cart.getItems().stream()
                    .map(CartItemResponse::new) // Ánh xạ đến CartItemResponse
                    .collect(Collectors.toList());
        
        // Tính tổng số lượng
        this.totalItems = cart.getItems().stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum();

        // Tính tổng tiền
        this.totalPrice = cart.getItems().stream()
                        .map(item -> item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

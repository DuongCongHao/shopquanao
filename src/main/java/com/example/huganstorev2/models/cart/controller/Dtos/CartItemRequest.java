package com.example.huganstorev2.models.cart.controller.Dtos;

import lombok.Data;

@Data 
public class CartItemRequest {
    private Long variantId;
    private Integer quantity;
}

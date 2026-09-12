package com.example.huganstorev2.models.cart.controller.Dtos;

import java.math.BigDecimal;

import com.example.huganstorev2.models.cart.entity.CartItem;

import lombok.Data;

@Data 
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private String productName;
    private String size;
    private String color;
    private String imgUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stock;

    public CartItemResponse(CartItem item){
        this.id = item.getId();
        this.variantId = item.getVariant().getId();
        this.productName = item.getVariant().getProduct().getName();
        this.size = item.getVariant().getSize();
        this.color = item.getVariant().getColor();
        this.imgUrl = item.getVariant().getImgUrl();
        this.price = item.getVariant().getPrice();
        this.quantity = item.getQuantity();
        this.subtotal = item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
        this.stock = item.getVariant().getStock();
    }
}

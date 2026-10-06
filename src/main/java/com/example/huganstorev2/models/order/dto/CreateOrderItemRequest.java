package com.example.huganstorev2.models.order.dto;

import lombok.Data;

@Data
public class CreateOrderItemRequest {
    private Long variantId;
    private Integer quantity;
    private String printType;
    private String note;
}
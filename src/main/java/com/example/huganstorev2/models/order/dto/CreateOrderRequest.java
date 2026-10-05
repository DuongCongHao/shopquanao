package com.example.huganstorev2.models.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private List<CreateOrderItemRequest> items;
}
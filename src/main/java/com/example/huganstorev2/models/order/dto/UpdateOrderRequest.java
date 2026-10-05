package com.example.huganstorev2.models.order.dto;

import lombok.Data;

@Data
public class UpdateOrderRequest {
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private String status;
}
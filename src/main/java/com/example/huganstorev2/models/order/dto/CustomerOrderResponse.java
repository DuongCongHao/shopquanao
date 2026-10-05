package com.example.huganstorev2.models.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.huganstorev2.models.order.entity.CustomerOrder;

import lombok.Data;

@Data
public class CustomerOrderResponse {
    private Long id;
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private String status;
    private BigDecimal totalPrice;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CustomerOrderItemResponse> items;

    public CustomerOrderResponse(CustomerOrder order) {
        id = order.getId();
        customerName = order.getCustomerName();
        customerAddress = order.getCustomerAddress();
        customerPhone = order.getCustomerPhone();
        customerEmail = order.getCustomerEmail();
        status = order.getStatus();
        totalPrice = order.getTotalPrice();
        totalItems = order.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
        createdAt = order.getCreatedAt();
        updatedAt = order.getUpdatedAt();
        items = order.getItems().stream().map(CustomerOrderItemResponse::new).toList();
    }
}
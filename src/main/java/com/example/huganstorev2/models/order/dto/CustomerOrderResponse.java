package com.example.huganstorev2.models.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    private Long createdAtEpoch;
    private Long updatedAtEpoch;
    private Long statusChangedAtEpoch;
    private Long autoDeleteAt;
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
        createdAtEpoch = toEpochMillis(createdAt);
        updatedAtEpoch = toEpochMillis(updatedAt);
        statusChangedAtEpoch = toEpochMillis(order.getStatusChangedAt());
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            LocalDateTime statusDate = order.getStatusChangedAt() != null
                ? order.getStatusChangedAt()
                : order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            if (statusDate != null) {
                autoDeleteAt = toEpochMillis(statusDate.plus(7, ChronoUnit.DAYS));
            }
        }
        items = order.getItems().stream().map(CustomerOrderItemResponse::new).toList();
    }

    private Long toEpochMillis(LocalDateTime dateTime) {
        return dateTime == null
            ? null
            : dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
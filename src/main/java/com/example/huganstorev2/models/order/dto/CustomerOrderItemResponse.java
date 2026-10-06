package com.example.huganstorev2.models.order.dto;

import java.math.BigDecimal;

import com.example.huganstorev2.models.order.entity.CustomerOrderItem;

import lombok.Data;

@Data
public class CustomerOrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String imgUrl;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal garmentPrice;
    private BigDecimal subtotal;
    private String printType;
    private String note;
    private BigDecimal printPrice;

    public CustomerOrderItemResponse(CustomerOrderItem item) {
        id = item.getId();
        productId = item.getProductId();
        variantId = item.getVariantId();
        productName = item.getProductName();
        imgUrl = item.getImgUrl();
        size = item.getSize();
        color = item.getColor();
        quantity = item.getQuantity();
        price = item.getUnitPrice();
        garmentPrice = item.getGarmentPrice() != null
            ? item.getGarmentPrice()
            : item.getUnitPrice();
        subtotal = item.getSubtotal();
        printType = item.getPrintType();
        note = item.getNote();
        printPrice = item.getPrintPrice();
    }
}
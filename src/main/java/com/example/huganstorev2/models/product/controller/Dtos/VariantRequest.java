package com.example.huganstorev2.models.product.controller.Dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data 
public class VariantRequest {
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private String imgUrl;
}

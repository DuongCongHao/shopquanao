package com.example.huganstorev2.models.product.controller.Dtos;

import java.math.BigDecimal;

import com.example.huganstorev2.models.product.entity.ProductVariant;

import lombok.Data;

@Data 
public class VariantResponse {
    private Long id;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private String imgUrl;

    public VariantResponse(ProductVariant variant){
        this.id=variant.getId();
        this.size=variant.getSize();
        this.color=variant.getColor();
        this.price=variant.getPrice();
        this.stock=variant.getStock();
        this.sku=variant.getSku();
        this.imgUrl=variant.getImgUrl();
    }
}

package com.example.huganstorev2.models.product.controller.Dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data 
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String imgUrl;
    private List<String> images;
    private Boolean isPublished;
    private Long categoryId;
    private List<Long> categoryIds;
    private List<VariantRequest> variants;
}

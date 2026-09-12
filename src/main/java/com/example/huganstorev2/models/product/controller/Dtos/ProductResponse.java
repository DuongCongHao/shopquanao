package com.example.huganstorev2.models.product.controller.Dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.huganstorev2.models.product.entity.Product;

import lombok.Data;

@Data 
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private String imgUrl;
    private Boolean isPublished;
    private Long categoryId;
    private String categoryName;
    private List<VariantResponse> variants;

    public ProductResponse(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.slug = product.getSlug();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imgUrl = product.getImgUrl();
        this.isPublished = product.getIsPublished();
        
        if(product.getCategory() != null){
            this.categoryId = product.getCategory().getId();
            this.categoryName = product.getCategory().getName();
        }

        if (product.getVariants() != null) {
            this.variants = product.getVariants().stream()
                    .map(VariantResponse::new)
                    .collect(Collectors.toList());
        }
    }
}

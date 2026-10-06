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
    private List<String> images;
    private Boolean isPublished;
    private Long categoryId;
    private String categoryName;
    private List<CategorySummary> categories;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private List<VariantResponse> variants;

    public ProductResponse(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.slug = product.getSlug();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.images = product.getImages();
        this.imgUrl = this.images != null && !this.images.isEmpty() ? this.images.get(0) : product.getImgUrl();
        this.isPublished = product.getIsPublished();
        
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            this.categories = product.getCategories().stream()
                    .map(CategorySummary::new)
                    .collect(Collectors.toList());
        } else if (product.getCategory() != null) {
            this.categories = List.of(new CategorySummary(product.getCategory()));
        } else {
            this.categories = List.of();
        }
        this.categoryIds = this.categories.stream()
                .map(CategorySummary::id)
                .collect(Collectors.toList());
        this.categoryNames = this.categories.stream()
                .map(CategorySummary::name)
                .collect(Collectors.toList());
        if (!this.categories.isEmpty()) {
            this.categoryId = this.categories.get(0).id();
            this.categoryName = this.categories.get(0).name();
        }

        if (product.getVariants() != null) {
            this.variants = product.getVariants().stream()
                    .map(VariantResponse::new)
                    .collect(Collectors.toList());
        }
    }
}

package com.example.huganstorev2.models.product.controller.Dtos;

import com.example.huganstorev2.models.category.entity.Category;

public record CategorySummary(Long id, String name) {
    public CategorySummary(Category category) {
        this(category.getId(), category.getName());
    }
}

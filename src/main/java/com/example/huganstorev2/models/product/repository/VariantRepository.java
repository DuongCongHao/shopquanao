package com.example.huganstorev2.models.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.huganstorev2.models.product.entity.ProductVariant;

import java.util.List;

public interface  VariantRepository extends JpaRepository<ProductVariant, Long>{
    List<ProductVariant> findByProductId(Long productId);
    void deleteByProductId(Long productId);
    boolean existsBySku(String sku);
}

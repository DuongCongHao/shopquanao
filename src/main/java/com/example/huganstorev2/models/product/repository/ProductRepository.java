package com.example.huganstorev2.models.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.huganstorev2.models.product.entity.Product;

public interface  ProductRepository extends JpaRepository<Product, Long>{
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}

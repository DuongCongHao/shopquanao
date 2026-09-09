package com.example.huganstorev2.models.category.repository;

import com.example.huganstorev2.models.category.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  CategoryRepository extends JpaRepository<Category, Long> {
    
}

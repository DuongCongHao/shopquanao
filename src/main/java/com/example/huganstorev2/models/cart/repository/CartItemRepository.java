package com.example.huganstorev2.models.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.huganstorev2.models.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long>{
    
}

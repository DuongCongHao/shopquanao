package com.example.huganstorev2.models.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.huganstorev2.models.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long>{
    Optional<Cart> findByUserId(Long userId);
}

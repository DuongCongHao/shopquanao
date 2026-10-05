package com.example.huganstorev2.models.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.huganstorev2.models.order.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
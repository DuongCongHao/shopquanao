package com.example.huganstorev2.models.order.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.huganstorev2.models.order.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT o FROM CustomerOrder o
        WHERE o.status IN ('COMPLETED', 'CANCELLED')
          AND COALESCE(o.statusChangedAt, o.updatedAt, o.createdAt) <= :cutoff
        """)
    List<CustomerOrder> findExpiredTerminalOrders(@Param("cutoff") LocalDateTime cutoff);
}
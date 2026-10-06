package com.example.huganstorev2.models.order.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_order_items")
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder customerOrder;

    private Long productId;
    private Long variantId;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String imgUrl;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal garmentPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;

    private String printType;

    @Column(columnDefinition = "TEXT")
    private String note;

    private BigDecimal printPrice;
}
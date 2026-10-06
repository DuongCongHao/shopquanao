package com.example.huganstorev2.models.order.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.huganstorev2.models.order.dto.CreateOrderRequest;
import com.example.huganstorev2.models.order.dto.CustomerOrderResponse;
import com.example.huganstorev2.models.order.dto.UpdateOrderRequest;
import com.example.huganstorev2.models.order.service.CustomerOrderService;

@RestController
@RequestMapping("/api/v1/orders")
public class CustomerOrderController {
    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<CustomerOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/printing-capability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Boolean>> getPrintingCapability() {
        return ResponseEntity.ok(Map.of("printingSupported", true));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CustomerOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CustomerOrderResponse> updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
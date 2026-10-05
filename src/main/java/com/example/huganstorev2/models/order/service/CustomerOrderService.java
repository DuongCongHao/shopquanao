package com.example.huganstorev2.models.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.huganstorev2.exception.ResourceNotFoundException;
import com.example.huganstorev2.models.order.dto.CreateOrderItemRequest;
import com.example.huganstorev2.models.order.dto.CreateOrderRequest;
import com.example.huganstorev2.models.order.dto.CustomerOrderResponse;
import com.example.huganstorev2.models.order.dto.UpdateOrderRequest;
import com.example.huganstorev2.models.order.entity.CustomerOrder;
import com.example.huganstorev2.models.order.entity.CustomerOrderItem;
import com.example.huganstorev2.models.order.repository.CustomerOrderRepository;
import com.example.huganstorev2.models.product.entity.ProductVariant;
import com.example.huganstorev2.models.product.repository.VariantRepository;

@Service
public class CustomerOrderService {
    private static final Logger LOGGER = Logger.getLogger(CustomerOrderService.class.getName());
    private static final int TERMINAL_ORDER_RETENTION_DAYS = 7;
    private static final List<String> ORDER_STATUSES = List.of("PENDING", "CONFIRMED", "SHIPPED", "COMPLETED", "CANCELLED");
    private static final Map<String, List<String>> ALLOWED_STATUS_TRANSITIONS = Map.of(
        "PENDING", List.of("CONFIRMED", "CANCELLED"),
        "CONFIRMED", List.of("SHIPPED", "CANCELLED"),
        "SHIPPED", List.of("COMPLETED", "CANCELLED"),
        "COMPLETED", List.of(),
        "CANCELLED", List.of()
    );

    private final CustomerOrderRepository orderRepository;
    private final VariantRepository variantRepository;

    public CustomerOrderService(CustomerOrderRepository orderRepository, VariantRepository variantRepository) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional
    public CustomerOrderResponse createOrder(CreateOrderRequest request) {
        requireText(request.getCustomerName(), "Vui lòng nhập họ tên.");
        requireText(request.getCustomerAddress(), "Vui lòng nhập địa chỉ.");
        requireText(request.getCustomerPhone(), "Vui lòng nhập số điện thoại.");
        requireText(request.getCustomerEmail(), "Vui lòng nhập email.");
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng cần có ít nhất một sản phẩm.");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerAddress(request.getCustomerAddress().trim());
        order.setCustomerPhone(request.getCustomerPhone().trim());
        order.setCustomerEmail(request.getCustomerEmail().trim());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatusChangedAt(order.getCreatedAt());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest requestedItem : request.getItems()) {
            if (requestedItem.getVariantId() == null || requestedItem.getQuantity() == null || requestedItem.getQuantity() < 1) {
                throw new IllegalArgumentException("Thông tin sản phẩm trong đơn hàng không hợp lệ.");
            }

            ProductVariant variant = variantRepository.findById(requestedItem.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm."));
            if (variant.getStock() < requestedItem.getQuantity()) {
                throw new IllegalArgumentException("Số lượng đặt vượt quá tồn kho của " + variant.getProduct().getName() + ".");
            }

            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedItem.getQuantity()));
            CustomerOrderItem item = new CustomerOrderItem();
            item.setProductId(variant.getProduct().getId());
            item.setVariantId(variant.getId());
            item.setProductName(variant.getProduct().getName());
            item.setImgUrl(variant.getImgUrl() != null && !variant.getImgUrl().isBlank()
                ? variant.getImgUrl()
                : variant.getProduct().getImages() != null && !variant.getProduct().getImages().isEmpty()
                    ? variant.getProduct().getImages().get(0)
                    : variant.getProduct().getImgUrl());
            item.setSize(variant.getSize());
            item.setColor(variant.getColor());
            item.setQuantity(requestedItem.getQuantity());
            item.setUnitPrice(variant.getPrice());
            item.setSubtotal(subtotal);
            order.addItem(item);

            variant.setStock(variant.getStock() - requestedItem.getQuantity());
            total = total.add(subtotal);
        }

        order.setTotalPrice(total);
        return new CustomerOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(CustomerOrderResponse::new).toList();
    }

    @Transactional
    public CustomerOrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        CustomerOrder order = findOrder(id);
        if (request.getCustomerName() != null) order.setCustomerName(request.getCustomerName().trim());
        if (request.getCustomerAddress() != null) order.setCustomerAddress(request.getCustomerAddress().trim());
        if (request.getCustomerPhone() != null) order.setCustomerPhone(request.getCustomerPhone().trim());
        if (request.getCustomerEmail() != null) order.setCustomerEmail(request.getCustomerEmail().trim());
        if (request.getStatus() != null) {
            String status = request.getStatus().trim().toUpperCase();
            if (!ORDER_STATUSES.contains(status)) throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ.");
            if (!status.equals(order.getStatus()) && !ALLOWED_STATUS_TRANSITIONS
                    .getOrDefault(order.getStatus(), List.of()).contains(status)) {
                throw new IllegalArgumentException("Không thể chuyển đơn " + order.getStatus() + " sang " + status + ".");
            }
            if (!status.equals(order.getStatus())) {
                if (status.equals("CANCELLED")) {
                    restoreInventory(order);
                }
                order.setStatusChangedAt(LocalDateTime.now());
            }
            order.setStatus(status);
        }
        order.setUpdatedAt(LocalDateTime.now());
        return new CustomerOrderResponse(orderRepository.save(order));
    }

    public void deleteOrder(Long id) {
        orderRepository.delete(findOrder(id));
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteExpiredTerminalOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(TERMINAL_ORDER_RETENTION_DAYS);
        List<CustomerOrder> expiredOrders = orderRepository.findExpiredTerminalOrders(cutoff);
        if (!expiredOrders.isEmpty()) {
            orderRepository.deleteAll(expiredOrders);
            LOGGER.info(() -> "Automatically deleted " + expiredOrders.size()
                + " completed or cancelled orders older than "
                + TERMINAL_ORDER_RETENTION_DAYS + " days.");
        }
    }

    private CustomerOrder findOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));
    }

    private void restoreInventory(CustomerOrder order) {
        order.getItems().forEach(item -> {
            if (item.getVariantId() == null) return;
            variantRepository.findById(item.getVariantId()).ifPresent(variant ->
                variant.setStock(variant.getStock() + item.getQuantity())
            );
        });
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
}
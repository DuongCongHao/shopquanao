package com.example.huganstorev2.models.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.huganstorev2.models.cart.controller.Dtos.CartItemRequest;
import com.example.huganstorev2.models.cart.controller.Dtos.CartResponse;
import com.example.huganstorev2.models.cart.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Lấy giỏ hàng người dùng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getCart(@RequestParam Long userId){
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping
    @Operation(summary = "Thêm sản phẩm giỏ hàng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addItemToCart(@RequestBody @Valid CartItemRequest request, @RequestParam Long userId){
        CartResponse response = cartService.AddItemToCart(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Cập nhật số lượng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateCartItems(
            @RequestParam Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity
    ){
        CartResponse response = cartService.updateCartItems(userId, itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa sản phẩm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeCartItem(@RequestParam Long userId, @PathVariable Long itemId){
        CartResponse response = cartService.removeCartItem(itemId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Xóa giỏ hàng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearCart(@RequestParam Long userId){
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}

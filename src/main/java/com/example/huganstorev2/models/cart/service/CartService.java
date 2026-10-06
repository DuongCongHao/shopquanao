package com.example.huganstorev2.models.cart.service;

import com.example.huganstorev2.models.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import com.example.huganstorev2.exception.ResourceNotFoundException;
import com.example.huganstorev2.models.cart.controller.Dtos.CartItemRequest;
import com.example.huganstorev2.models.cart.controller.Dtos.CartResponse;
import com.example.huganstorev2.models.cart.entity.Cart;
import com.example.huganstorev2.models.cart.entity.CartItem;
import com.example.huganstorev2.models.cart.repository.CartItemRepository;
import com.example.huganstorev2.models.cart.repository.CartRepository;
import com.example.huganstorev2.models.product.entity.ProductVariant;
import com.example.huganstorev2.models.product.repository.VariantRepository;
import com.example.huganstorev2.models.user.entity.User;
import com.example.huganstorev2.models.user.repository.UserRepository;

@Service 
public class CartService {
    private final CartRepository cartRepository;
    private final VariantRepository variantRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    public CartService(CartRepository cartRepository
        , VariantRepository variantRepository, CartItemRepository cartItemRepository
        , UserRepository userRepository, CategoryRepository categoryRepository){
        this.cartRepository = cartRepository;
        this.variantRepository = variantRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }
    // Lấy giỏ hàng của user
    public CartResponse getCartByUserId(Long userId){
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy người dùng!")
        );
        Cart cart = user.getCart();
        if(cart == null){
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }
        return new CartResponse(cart);
    }
    // Thêm giỏ hàng
    public CartResponse AddItemToCart(Long userId, CartItemRequest request){
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy người dùng!")
        );
        Cart cart = user.getCart();
        if(cart == null){
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        ProductVariant variant = variantRepository.findById(request.getVariantId())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));

        if(request.getQuantity() == null || request.getQuantity() <= 0){
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        CartItem existingItem = cart.getItems().stream()
                                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                                .findFirst()
                                .orElse(null);
        int newQuantity;
        CartItem itemToSave;

        if(existingItem != null){
            newQuantity = existingItem.getQuantity() + request.getQuantity();
            itemToSave = existingItem;
        } else {
            newQuantity = request.getQuantity();
            CartItem newItem = new CartItem();
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            cart.addItem(newItem);
            itemToSave = newItem;
        }

        itemToSave.setQuantity(newQuantity);

        cartItemRepository.save(itemToSave);

        Cart savedCart = cartRepository.save(cart);
        return new CartResponse(savedCart);
    }
    // Cập nhật số lượng
    public CartResponse updateCartItems(Long userId, Long itemId, Integer quantity){
        CartItem cartItem = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));
        // Kiểm tra quyền sở hữu
        if(!cartItem.getCart().getUser().getId().equals(userId)){
            throw new RuntimeException("Không có quyền thao tác!");
        }

        if(quantity == null || quantity <= 0){
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        return new CartResponse(cart);
    }
    // Xóa sản phẩm
    public CartResponse removeCartItem(Long itemId, Long userId){
        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!")
        );
        if(!cartItem.getCart().getUser().getId().equals(userId)){
            throw new RuntimeException("Không có quyền thao tác!");
        }
        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem); // Helper method trong entity

        Cart savedCart = cartRepository.save(cart);

        return new CartResponse(savedCart);
    }
    // Xóa giỏ hàng
    public void clearCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy giỏ hangg!")
        );
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}

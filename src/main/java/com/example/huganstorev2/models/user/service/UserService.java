package com.example.huganstorev2.models.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.huganstorev2.models.cart.entity.Cart;
import com.example.huganstorev2.models.user.entity.*;
import com.example.huganstorev2.models.user.repository.UserRepository;
import com.example.huganstorev2.models.user.service.Dtos.LoginRequest;
import com.example.huganstorev2.models.user.service.Dtos.RegisterRequest;

import jakarta.transaction.Transactional;

import java.util.List;

@Service 
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // Kiểm tra quyền
    public void checkAdminRole(Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        if(user.getRole() == null || user.getRole() != Role.ADMIN){
            throw new RuntimeException("Không có quyền Admin!");
        }
    }
    // Đăng ký
    @Transactional 
    public User registerUser(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email đã tồn tại!");
        }
        if(userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setFullName(request.getFullName());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        
        return userRepository.save(user);
    }
    // Đăng nhập
    public User loginUser(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }
        return user;
    }
    // Lấy toàn bộ danh sách người dùng
    public List<User> getAllUser(){
        return userRepository.findAll();
    }
    // Lấy người dùng theo email
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }    
}

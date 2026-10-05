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
    public static final int SESSION_TIMEOUT_MINUTES = 5;

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
    @Transactional
    public User loginUser(LoginRequest request, String sessionId){
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }

        LocalDateTime now = LocalDateTime.now();
        int claimed = userRepository.claimSession(
            user.getId(),
            sessionId,
            now,
            now.minusMinutes(SESSION_TIMEOUT_MINUTES)
        );
        if (claimed == 0) {
            throw new RuntimeException("Tài khoản đang hoạt động trên thiết bị khác.");
        }
        return user;
    }

    @Transactional
    public boolean refreshSession(String email, String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.refreshSession(
            email,
            sessionId,
            now,
            now.minusMinutes(SESSION_TIMEOUT_MINUTES)
        ) > 0;
    }

    @Transactional
    public void releaseSession(String email, String sessionId) {
        userRepository.releaseSession(email, sessionId);
    }

    // Đổi mật khẩu cho tài khoản đang đăng nhập
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword){
        if(newPassword == null || newPassword.length() < 6){
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }
        User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        if(currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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

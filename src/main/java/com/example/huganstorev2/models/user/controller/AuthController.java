package com.example.huganstorev2.models.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.example.huganstorev2.config.JwtService;
import com.example.huganstorev2.models.user.entity.User;
import com.example.huganstorev2.models.user.service.UserService;
import com.example.huganstorev2.models.user.service.Dtos.LoginRequest;
import com.example.huganstorev2.models.user.service.Dtos.LoginResponse;
import com.example.huganstorev2.models.user.service.Dtos.RegisterRequest;
import com.example.huganstorev2.models.user.service.Dtos.ChangePasswordRequest;

import io.swagger.v3.oas.annotations.Operation;

@RestController 
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    public AuthController(UserService userService, JwtService jwtService){
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Đăng ký (Không cần token)
    @PostMapping("/register")
    @Operation (summary = "Đăng ký")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request){
        try{
            User user = userService.registerUser(request);
            user.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Đăng nhập (Không cần token)
    @PostMapping("/login")
    @Operation (summary = "Đăng nhập")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request){
        try {
            User user = userService.loginUser(request);
            // Tạo token
            String token = jwtService.generateToken(user.getEmail(), user.getRole());
            LoginResponse response = new LoginResponse(
                "Đăng nhập thành công",
                token,
                user
            );
            return ResponseEntity.ok(response);
        } catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu tài khoản đang đăng nhập")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập lại!");
            }
            String email = (String) auth.getPrincipal();
            if (email == null || email.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập lại!");
            }
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

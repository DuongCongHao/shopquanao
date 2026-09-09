package com.example.huganstorev2.models.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.huganstorev2.models.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

@RestController 
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    // Lấy danh sách người dùng
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation (summary = "Lấy danh sách người dùng - dành cho ADMIN")
    public ResponseEntity<?> getAllUser(){
        return ResponseEntity.ok(userService.getAllUser());
    }
}

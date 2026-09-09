package com.example.huganstorev2.models.user.service.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.example.huganstorev2.models.user.entity.*;

@Data 
@AllArgsConstructor 
public class LoginResponse {
    private String message;
    private String token;
    private String email;
    private String password;
    private Long userId;
    private String fullName;
    private Role role;

    public LoginResponse(String token, String message, User user){
        this.message = message;
        this.userId = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.token = token;
    }
}

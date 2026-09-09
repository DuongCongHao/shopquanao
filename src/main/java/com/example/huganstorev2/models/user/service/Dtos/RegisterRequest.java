package com.example.huganstorev2.models.user.service.Dtos;

import lombok.Data;

@Data 
public class RegisterRequest {
    private String email;
    private String phone;
    private String fullName;
    private String password;
}

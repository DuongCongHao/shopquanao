package com.example.huganstorev2.models.user.service.Dtos;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
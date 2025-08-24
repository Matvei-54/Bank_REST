package com.example.bankcards.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password) {
}

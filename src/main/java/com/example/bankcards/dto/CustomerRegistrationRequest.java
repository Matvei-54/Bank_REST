package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public record CustomerRegistrationRequest(
        @Schema(description = "Имя пользователя")
        @NotNull(message = "Name should not be empty.")
        @Size(min = 6, max = 50, message = "FirstName should between 6 and 50 characters.")
        String name,

        @Schema(description = "Email пользователя")
        @Email
        @NotNull(message = "Email should not be empty.")
        @Size(min = 6, max = 50, message = "Email should between 6 and 50 characters.")
        String email,

        @Schema(description = "Пароль пользователя")
        @NotNull(message = "Password should not be empty.")
        @Size(min = 6, max = 50, message = "Email should between 6 and 50 characters.")
        String password

) {
}

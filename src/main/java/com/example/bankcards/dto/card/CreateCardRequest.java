package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.*;

import javax.validation.constraints.*;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @Schema(description = "Email пользователя")
        @Email
        @NotNull(message = "Email should not be empty.")
        @Size(min = 6, max = 50, message = "Email should between 6 and 50 characters.")
        String cardOwner,

        @NotNull
        @Future(message = "Date expiry must be the future")
        LocalDate expiryDate
) {
}

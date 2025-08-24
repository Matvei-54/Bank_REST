package com.example.bankcards.dto.card;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public record SetLimitsForCardRequest(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal dailyLimit,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal monthlyLimit
) {
}

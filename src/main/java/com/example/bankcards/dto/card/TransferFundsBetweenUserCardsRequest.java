package com.example.bankcards.dto.card;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public record TransferFundsBetweenUserCardsRequest(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String fromCardNumber,

        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String toCardNumber,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotNull
        String currency
) {
}

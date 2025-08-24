package com.example.bankcards.dto.card;

import java.math.BigDecimal;

public record ReplenishmentCardRequest(
        String cardNumber,
        BigDecimal amount
) {
}

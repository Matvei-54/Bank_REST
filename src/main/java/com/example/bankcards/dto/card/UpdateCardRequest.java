package com.example.bankcards.dto.card;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

public record UpdateCardRequest(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String newCardNumber,


        @NotNull
        @Future(message = "Date expiry must be the future")
        LocalDate newExpiryDate

) {
}

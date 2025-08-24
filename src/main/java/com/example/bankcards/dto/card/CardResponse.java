package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CardResponse {

    private String cardNumber;
    private String cardHolder;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;
    private String currency;
}

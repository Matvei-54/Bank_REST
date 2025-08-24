package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class CardNumberEncryptorConverter implements AttributeConverter<String, String> {

    private final CardNumberEncryptorUtil cardNumberEncryptorUtil;

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumberEncryptorUtil.encryptCardNumber(cardNumber);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return cardNumberEncryptorUtil.decryptCardNumber(dbData);
    }
}

package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CardStatusConverter implements AttributeConverter<CardStatus, String> {

    @Override
    public String convertToDatabaseColumn(CardStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CardStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CardStatus.fromString(dbData);
    }
}

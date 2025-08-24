package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.entity.enums.LimitType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LimitTypeConverter implements AttributeConverter<LimitType, String> {

    @Override
    public String convertToDatabaseColumn(LimitType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public LimitType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LimitType.fromString(dbData);
    }
}

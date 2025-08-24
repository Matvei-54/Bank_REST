package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardHolder", expression = "java(card.getCustomer().getName())")
    @Mapping(target = "cardNumber", source = "cardNumber", qualifiedByName = "convertCardNumberToMask")
    CardResponse toCardResponse(Card card);


    @Named("convertCardNumberToMask")
    default String convertCardNumberToMask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return cardNumber;
        String lastFourDigit = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigit;
    }
}

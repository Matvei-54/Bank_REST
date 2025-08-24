package com.example.bankcards.exception.card;

public class CardWithNumberAlreadyExistsException extends RuntimeException {

    public CardWithNumberAlreadyExistsException(String cardNumber){
        super(String.format("Card with number %s already exists", cardNumber));
    }
}

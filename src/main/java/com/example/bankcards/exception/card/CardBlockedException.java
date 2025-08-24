package com.example.bankcards.exception.card;

public class CardBlockedException extends RuntimeException{

    public CardBlockedException(){
        super("One or both cards blocked");
    }
}

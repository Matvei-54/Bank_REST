package com.example.bankcards.exception.card;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(){
        super("Insufficient Funds");
    }
}

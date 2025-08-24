package com.example.bankcards.exception.customer;

public class NoAccessToOtherDataException extends RuntimeException{

    public NoAccessToOtherDataException() {
        super("You cannot request data that does not belong to you.");
    }
}

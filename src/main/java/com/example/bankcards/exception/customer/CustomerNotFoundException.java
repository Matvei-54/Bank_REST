package com.example.bankcards.exception.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super(String.format("Customer with id %s not found", id));
    }

    public CustomerNotFoundException(String email) {
        super(String.format("Customer with email %s not found", email));
    }
}

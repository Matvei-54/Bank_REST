package com.example.bankcards.exception.customer;

public class CustomerAlreadyRegisteredException extends RuntimeException {

    public CustomerAlreadyRegisteredException(String emailCustomer) {
        super(String.format("Customer with email %s already registered", emailCustomer));
    }
}

package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionType {

    DEBIT, CREDIT, TRANSFER, WITHDRAWAL;

    public static TransactionType fromString(String transactionType) {
        for (TransactionType type : TransactionType.values()) {
            if(type.toString().equals(transactionType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid card status: " + transactionType);
    }
}

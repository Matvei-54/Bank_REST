package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum CardStatus {

    ACTIVE,BLOCKED,EXPIRED;

    public static CardStatus fromString(String cardStatus) {
        for (CardStatus status : CardStatus.values()) {
            if(status.toString().equals(cardStatus)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid card status: " + cardStatus);
    }
}

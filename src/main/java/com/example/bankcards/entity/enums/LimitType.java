package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum LimitType {

    DAILY, MONTHLY, YEAR;

    public static LimitType fromString(String limit) {
        for (LimitType limitType : LimitType.values()) {
            if(limitType.toString().equals(limit)) {
                return limitType;
            }
        }
        throw new IllegalArgumentException("Invalid limitType value: " + limit);
    }
}

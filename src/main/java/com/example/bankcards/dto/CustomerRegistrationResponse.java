package com.example.bankcards.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class CustomerRegistrationResponse {

    private String name;

    private String email;
}

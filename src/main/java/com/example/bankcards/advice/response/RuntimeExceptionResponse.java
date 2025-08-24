package com.example.bankcards.advice.response;

import lombok.*;

import java.time.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RuntimeExceptionResponse {

    private String message;
    private LocalDateTime timestamp;
}

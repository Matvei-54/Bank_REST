package com.example.bankcards.advice;

import com.example.bankcards.advice.response.*;
import com.example.bankcards.exception.customer.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.validation.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestControllerAdvice
public class CustomerExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    private RuntimeExceptionResponse error(RuntimeException e){
        return new RuntimeExceptionResponse(e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomerAlreadyRegisteredException.class)
    private RuntimeExceptionResponse customerAlreadyRegistered(CustomerAlreadyRegisteredException e){
        return new RuntimeExceptionResponse(e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomerNotFoundException.class)
    private RuntimeExceptionResponse customerNotFound(CustomerNotFoundException e){
        return new RuntimeExceptionResponse(e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private Map<String, String> error(MethodArgumentNotValidException exception){
        Map<String,String> errorValid = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(
                errors -> {
                    String field = ((FieldError)errors).getField();
                    String errorMessage = errors.getDefaultMessage();
                    errorValid.put(field, errorMessage);
                }
        );
        return errorValid;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public RuntimeExceptionResponse handleAccessDenied(AccessDeniedException e) {
        return new RuntimeExceptionResponse(e.getMessage(), LocalDateTime.now());
    }
}

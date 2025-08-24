package com.example.bankcards.controller;


import com.example.bankcards.dto.CustomerRegistrationRequest;
import com.example.bankcards.dto.CustomerRegistrationResponse;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CustomerService;
import com.example.bankcards.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RequiredArgsConstructor
@RequestMapping("/api/customer")
@RestController
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;
    private final IdempotencyService idempotencyService;

    /**
     * Запрос регистрации нового пользователя
     * @param customerReqDto dto с параметрами нового пользователя
     * @param idempotencyKey
     * @return dto с параметрами созданного пользователя
     */
    @Operation(summary = "Зарегистрировать нового пользователя", description = "В ответе возвращается dto.")
    @Tag(name = "sign up", description = "Customer")
    @PostMapping("/registration")
    public CustomerRegistrationResponse customerRegistration(@Valid @RequestBody CustomerRegistrationRequest customerReqDto,
                                                             @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey){
        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, CustomerRegistrationResponse.class);
        }
        return customerService.registerCustomer(customerReqDto, idempotencyKey);
    }
}

package com.example.bankcards.service;

import com.example.bankcards.dto.CustomerRegistrationRequest;
import com.example.bankcards.dto.CustomerRegistrationResponse;
import com.example.bankcards.entity.Customer;
import com.example.bankcards.entity.mapper.CustomerMapper;
import com.example.bankcards.exception.customer.CustomerAlreadyRegisteredException;
import com.example.bankcards.repository.CustomerRepository;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final RoleRepository roleRepository;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    private final IdempotencyService idempotencyService;
    private final long TIME_LIFE_RECORD_DB = 3600;


    /**
     * Метод создание пользователя
     * @return dto зарегистрированного пользователя
     */
    @Transactional
    public CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest customerDto, String idempotencyKey) {
        if(customerRepository.findByEmail(customerDto.email()).isPresent()) {
            log.error("Customer with email {} already exists", customerDto.email());
            throw new CustomerAlreadyRegisteredException(customerDto.email());
        }

        Customer customer = new Customer();
        customer.setEmail(customerDto.email());
        customer.setPassword(argon2PasswordEncoder.encode(customerDto.password()));
        customer.setName(customerDto.name());
        customer.setRoles(Collections.singleton(roleRepository.findByName("USER").get()));
        customer.setAccountNonExpired(true);
        customer.setAccountNonLocked(true);
        customer.setCredentialsNonExpired(true);
        customer.setEnabled(true);
        CustomerRegistrationResponse response = customerMapper
                .toCustomerRegistrationResponse(customerRepository.save(customer));
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);
        return response;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

}

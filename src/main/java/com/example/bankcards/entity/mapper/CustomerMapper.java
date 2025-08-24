package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.CustomerRegistrationResponse;
import com.example.bankcards.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerRegistrationResponse toCustomerRegistrationResponse(Customer customer);
}

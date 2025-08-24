package com.example.bankcards.service;

import com.example.bankcards.entity.Customer;
import com.example.bankcards.entity.app_class.CustomerDetails;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerDetailService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(username).orElseThrow(()-> new CustomerNotFoundException(username));

        return new CustomerDetails(customer);
    }
}

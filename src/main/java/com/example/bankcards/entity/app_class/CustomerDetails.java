package com.example.bankcards.entity.app_class;

import com.example.bankcards.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CustomerDetails implements UserDetails {

    private Customer customer;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return customer.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return customer.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return customer.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return customer.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return customer.isEnabled();
    }
}

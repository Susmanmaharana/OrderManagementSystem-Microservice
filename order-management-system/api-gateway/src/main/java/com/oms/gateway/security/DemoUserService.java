package com.oms.gateway.security;

import com.oms.gateway.config.SecurityProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DemoUserService {

    private final SecurityProperties securityProperties;

    public DemoUserService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public Optional<SecurityProperties.UserAccount> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        for (SecurityProperties.UserAccount user : securityProperties.getUsers()) {
            if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}

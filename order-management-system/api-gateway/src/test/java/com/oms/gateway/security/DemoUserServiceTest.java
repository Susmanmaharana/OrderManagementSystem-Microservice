package com.oms.gateway.security;

import com.oms.gateway.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoUserServiceTest {

    private DemoUserService demoUserService;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        SecurityProperties.UserAccount user = new SecurityProperties.UserAccount();
        user.setUsername("customer");
        user.setPassword("customer123");
        user.setRole("CUSTOMER");
        properties.setUsers(Collections.singletonList(user));
        demoUserService = new DemoUserService(properties);
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        assertTrue(demoUserService.authenticate("customer", "customer123").isPresent());
        assertEquals("CUSTOMER", demoUserService.authenticate("customer", "customer123").get().getRole());
    }

    @Test
    void authenticate_invalidPassword_empty() {
        assertFalse(demoUserService.authenticate("customer", "wrong").isPresent());
    }
}

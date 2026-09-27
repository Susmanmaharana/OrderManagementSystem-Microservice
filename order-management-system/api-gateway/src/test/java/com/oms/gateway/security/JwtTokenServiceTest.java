package com.oms.gateway.security;

import com.oms.gateway.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret("unit-test-secret-key-at-least-32-bytes!!");
        properties.getJwt().setExpirationMs(60_000L);
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void createAndParseToken_roundTrip() {
        String token = jwtTokenService.createToken("customer", "CUSTOMER");
        Claims claims = jwtTokenService.parseClaims(token);

        assertEquals("customer", claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role", String.class));
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }

    @Test
    void parse_invalidToken_throws() {
        assertThrows(Exception.class, () -> jwtTokenService.parseClaims("not.a.jwt"));
    }
}

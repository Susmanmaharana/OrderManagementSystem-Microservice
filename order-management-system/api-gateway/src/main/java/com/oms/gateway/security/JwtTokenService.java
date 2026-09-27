package com.oms.gateway.security;

import com.oms.gateway.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenService {

    private final SecurityProperties securityProperties;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + securityProperties.getJwt().getExpirationMs());
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretBytes())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationMs() {
        return securityProperties.getJwt().getExpirationMs();
    }

    private byte[] secretBytes() {
        return securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }
}

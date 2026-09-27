package com.oms.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Reads Bearer JWT and populates the reactive security context.
 * Does not reject invalid tokens here — SecurityWebFilterChain enforces authz.
 */
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return chain.filter(exchange);
        }

        try {
            Claims claims = jwtTokenService.parseClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (username == null || role == null) {
                return chain.filter(exchange);
            }

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role));
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } catch (Exception ex) {
            // Leave unauthenticated; authorizeExchange will return 401 when auth is required
            return chain.filter(exchange);
        }
    }
}

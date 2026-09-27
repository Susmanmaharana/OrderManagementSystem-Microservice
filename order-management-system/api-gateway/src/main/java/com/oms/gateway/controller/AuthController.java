package com.oms.gateway.controller;

import com.oms.gateway.config.SecurityProperties;
import com.oms.gateway.dto.ErrorResponse;
import com.oms.gateway.dto.LoginRequest;
import com.oms.gateway.dto.LoginResponse;
import com.oms.gateway.security.DemoUserService;
import com.oms.gateway.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final DemoUserService demoUserService;
    private final JwtTokenService jwtTokenService;

    public AuthController(DemoUserService demoUserService, JwtTokenService jwtTokenService) {
        this.demoUserService = demoUserService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@Valid @RequestBody LoginRequest request, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> demoUserService.authenticate(request.getUsername(), request.getPassword())
                .map(user -> {
                    LoginResponse response = new LoginResponse();
                    response.setAccessToken(jwtTokenService.createToken(user.getUsername(), user.getRole()));
                    response.setRole(user.getRole());
                    response.setExpiresInMs(jwtTokenService.getExpirationMs());
                    return ResponseEntity.ok((Object) response);
                })
                .orElseGet(() -> {
                    ErrorResponse error = new ErrorResponse();
                    error.setTimestamp(LocalDateTime.now());
                    error.setStatus(HttpStatus.UNAUTHORIZED.value());
                    error.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                    error.setMessage("Invalid username or password");
                    error.setPath(exchange.getRequest().getPath().value());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body((Object) error);
                }));
    }
}

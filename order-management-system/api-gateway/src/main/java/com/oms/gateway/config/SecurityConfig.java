package com.oms.gateway.config;

import com.oms.gateway.security.JwtAuthenticationFilter;
import com.oms.gateway.security.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                .and()
                .authorizeExchange()
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                    .pathMatchers(HttpMethod.PUT, "/api/v1/inventory/*").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/v1/inventory/reserve").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/v1/inventory/release").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/v1/payments/**").hasRole("ADMIN")
                    .pathMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                    .pathMatchers(HttpMethod.GET, "/api/v1/inventory/**").hasAnyRole("CUSTOMER", "ADMIN")
                    .pathMatchers("/api/v1/payments/**").hasAnyRole("CUSTOMER", "ADMIN")
                    .pathMatchers("/actuator/gateway/**").hasRole("ADMIN")
                    .anyExchange().authenticated()
                .and()
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}

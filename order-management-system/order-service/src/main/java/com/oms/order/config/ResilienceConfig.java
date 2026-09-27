package com.oms.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    public static final String PAYMENT_SERVICE = "paymentService";

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .failureRateThreshold(50)
                .recordExceptions(com.oms.order.exception.PaymentServiceException.class, ResourceAccessException.class)
                .ignoreExceptions(com.oms.order.exception.PaymentNonRetryableException.class)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .retryExceptions(com.oms.order.exception.PaymentServiceException.class, ResourceAccessException.class)
                .ignoreExceptions(com.oms.order.exception.PaymentNonRetryableException.class)
                .build();
        return RetryRegistry.of(config);
    }
}

package com.oms.order.client;

import com.oms.order.config.PaymentClientProperties;
import com.oms.order.exception.PaymentNonRetryableException;
import com.oms.order.exception.PaymentServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PaymentClientProperties properties;

    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        when(properties.getBaseUrl()).thenReturn("http://localhost:8083");
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        paymentClient = new PaymentClient(restTemplate, properties, circuitBreakerRegistry, retryRegistry);
    }

    @Test
    void doPay_success() {
        PaymentResponse body = new PaymentResponse();
        body.setPaymentId("PAY-00001");
        body.setStatus("SUCCESS");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(PaymentResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        PaymentResponse response = paymentClient.doPay(1L, new BigDecimal("100.00"), "key", false);
        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void doPay_5xx_isRetryablePaymentServiceException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(PaymentResponse.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "down",
                        null, new byte[0], StandardCharsets.UTF_8));

        assertThrows(PaymentServiceException.class,
                () -> paymentClient.doPay(1L, new BigDecimal("100.00"), "key", false));
    }

    @Test
    void doPay_4xx_isNonRetryable() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(PaymentResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "bad",
                        null, new byte[0], StandardCharsets.UTF_8));

        assertThrows(PaymentNonRetryableException.class,
                () -> paymentClient.doPay(1L, new BigDecimal("100.00"), "key", false));
    }

    @Test
    void doPay_timeout_isRetryable() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(PaymentResponse.class)))
                .thenThrow(new ResourceAccessException("read timed out", new SocketTimeoutException()));

        assertThrows(PaymentServiceException.class,
                () -> paymentClient.doPay(1L, new BigDecimal("100.00"), "key", false));
    }
}

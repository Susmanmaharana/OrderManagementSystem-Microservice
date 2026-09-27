package com.oms.order.client;

import com.oms.order.config.PaymentClientProperties;
import com.oms.order.config.ResilienceConfig;
import com.oms.order.exception.PaymentNonRetryableException;
import com.oms.order.exception.PaymentServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.function.Supplier;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public PaymentClient(RestTemplate paymentRestTemplate,
                         PaymentClientProperties properties,
                         CircuitBreakerRegistry circuitBreakerRegistry,
                         RetryRegistry retryRegistry) {
        this.restTemplate = paymentRestTemplate;
        this.properties = properties;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConfig.PAYMENT_SERVICE);
        this.retry = retryRegistry.retry(ResilienceConfig.PAYMENT_SERVICE);
    }

    /**
     * Timeout via RestTemplate read/connect timeouts.
     * Retry then CircuitBreaker (decorate order: CB wraps Retry wraps call).
     */
    public PaymentResponse pay(Long orderId, BigDecimal amount, String idempotencyKey, boolean forceFailure) {
        Supplier<PaymentResponse> supplier = new Supplier<PaymentResponse>() {
            @Override
            public PaymentResponse get() {
                return doPay(orderId, amount, idempotencyKey, forceFailure);
            }
        };

        Supplier<PaymentResponse> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, supplier));

        try {
            return decorated.get();
        } catch (CallNotPermittedException ex) {
            log.error("Payment circuit OPEN for orderId={}", orderId);
            throw new PaymentServiceException(
                    "Payment unavailable (circuit breaker open): " + ex.getMessage(), ex);
        } catch (PaymentNonRetryableException ex) {
            throw ex;
        } catch (PaymentServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PaymentServiceException(
                    "Payment unavailable (resilience): " + ex.getMessage(), ex);
        }
    }

    PaymentResponse doPay(Long orderId, BigDecimal amount, String idempotencyKey, boolean forceFailure) {
        String url = properties.getBaseUrl() + "/api/v1/payments";
        PaymentRequest body = new PaymentRequest(orderId, amount, idempotencyKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (forceFailure) {
            headers.set("X-Force-Payment-Failure", "true");
        }

        try {
            log.info("Calling payment for orderId={} amount={} forceFailure={}", orderId, amount, forceFailure);
            ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<PaymentRequest>(body, headers),
                    PaymentResponse.class);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            HttpStatus status = ex.getStatusCode();
            String detail = "Payment service error " + status.value() + ": " + ex.getResponseBodyAsString();
            if (status.is5xxServerError()) {
                throw new PaymentServiceException(detail);
            }
            throw new PaymentNonRetryableException(detail);
        } catch (ResourceAccessException ex) {
            throw new PaymentServiceException("Payment service unavailable: " + ex.getMessage(), ex);
        }
    }
}

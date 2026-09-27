package com.oms.order.exception;

/**
 * Payment errors that must not be retried (e.g. 4xx from payment service).
 */
public class PaymentNonRetryableException extends PaymentServiceException {

    public PaymentNonRetryableException(String message) {
        super(message);
    }
}

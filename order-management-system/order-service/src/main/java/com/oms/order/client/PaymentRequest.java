package com.oms.order.client;

import java.math.BigDecimal;

public class PaymentRequest {

    private Long orderId;
    private BigDecimal amount;
    private String idempotencyKey;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, BigDecimal amount, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}

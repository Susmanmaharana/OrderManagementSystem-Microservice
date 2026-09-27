package com.oms.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.payment")
public class PaymentProperties {

    /** 0.0 = never fail, 1.0 = always fail (simulated). */
    private double failureRate = 0.0;

    /** Artificial processing delay for timeout demos. */
    private long delayMs = 0L;

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }
}

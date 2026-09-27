package com.oms.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "oms.order")
public class OrderProperties {

    private BigDecimal defaultUnitPrice = new BigDecimal("100.00");

    public BigDecimal getDefaultUnitPrice() {
        return defaultUnitPrice;
    }

    public void setDefaultUnitPrice(BigDecimal defaultUnitPrice) {
        this.defaultUnitPrice = defaultUnitPrice;
    }
}

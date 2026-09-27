package com.oms.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.inventory")
public class InventoryProperties {

    private int optimisticLockMaxRetries = 3;

    public int getOptimisticLockMaxRetries() {
        return optimisticLockMaxRetries;
    }

    public void setOptimisticLockMaxRetries(int optimisticLockMaxRetries) {
        this.optimisticLockMaxRetries = optimisticLockMaxRetries;
    }
}

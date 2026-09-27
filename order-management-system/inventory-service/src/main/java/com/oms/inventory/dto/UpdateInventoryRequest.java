package com.oms.inventory.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateInventoryRequest {

    @NotNull
    @Min(0)
    private Integer availableQuantity;

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}

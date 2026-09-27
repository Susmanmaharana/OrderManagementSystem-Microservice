package com.oms.inventory.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class ReserveRequest {

    @NotNull
    @Positive
    private Long orderId;

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

package com.oms.order.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class OrderItemRequest {

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;

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

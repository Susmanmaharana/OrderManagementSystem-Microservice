package com.oms.inventory.dto;

public class InventoryResponse {

    private Long productId;
    private Integer availableQuantity;
    private Long version;

    public InventoryResponse() {
    }

    public InventoryResponse(Long productId, Integer availableQuantity, Long version) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.version = version;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

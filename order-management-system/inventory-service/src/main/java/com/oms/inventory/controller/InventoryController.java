package com.oms.inventory.controller;

import com.oms.inventory.dto.InventoryResponse;
import com.oms.inventory.dto.ReservationResponse;
import com.oms.inventory.dto.ReserveRequest;
import com.oms.inventory.dto.UpdateInventoryRequest;
import com.oms.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get stock for a product")
    public InventoryResponse getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Create or update stock quantity")
    public InventoryResponse upsertInventory(@PathVariable Long productId,
                                             @Valid @RequestBody UpdateInventoryRequest request) {
        return inventoryService.upsertInventory(productId, request);
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/release")
    @Operation(summary = "Release previously reserved stock")
    public ReservationResponse release(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.release(request);
    }
}

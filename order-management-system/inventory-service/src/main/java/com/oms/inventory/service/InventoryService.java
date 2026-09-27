package com.oms.inventory.service;

import com.oms.inventory.config.InventoryProperties;
import com.oms.inventory.dto.InventoryResponse;
import com.oms.inventory.dto.ReservationResponse;
import com.oms.inventory.dto.ReserveRequest;
import com.oms.inventory.dto.UpdateInventoryRequest;
import com.oms.inventory.entity.Inventory;
import com.oms.inventory.exception.ConcurrentInventoryUpdateException;
import com.oms.inventory.exception.ResourceNotFoundException;
import com.oms.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryCommandService commandService;
    private final InventoryProperties properties;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryCommandService commandService,
                            InventoryProperties properties) {
        this.inventoryRepository = inventoryRepository;
        this.commandService = commandService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {
        Inventory inventory = findInventoryOrThrow(productId);
        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse upsertInventory(Long productId, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(productId).orElseGet(() -> {
            Inventory created = new Inventory();
            created.setProductId(productId);
            return created;
        });
        inventory.setAvailableQuantity(request.getAvailableQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Upserted productId={} quantity={}", productId, saved.getAvailableQuantity());
        return toResponse(saved);
    }

    public ReservationResponse reserve(ReserveRequest request) {
        return executeWithOptimisticRetry(() -> commandService.reserve(request));
    }

    public ReservationResponse release(ReserveRequest request) {
        return executeWithOptimisticRetry(() -> commandService.release(request));
    }

    private ReservationResponse executeWithOptimisticRetry(InventoryAction action) {
        int maxAttempts = Math.max(1, properties.getOptimisticLockMaxRetries());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.execute();
            } catch (OptimisticLockingFailureException ex) {
                log.warn("Optimistic lock conflict attempt {}/{}", attempt, maxAttempts);
                if (attempt == maxAttempts) {
                    throw new ConcurrentInventoryUpdateException(
                            "Could not update inventory after " + maxAttempts
                                    + " attempts due to concurrent updates");
                }
            }
        }
        throw new ConcurrentInventoryUpdateException("Could not update inventory due to concurrent updates");
    }

    private Inventory findInventoryOrThrow(Long productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(inventory.getProductId(), inventory.getAvailableQuantity(), inventory.getVersion());
    }

    @FunctionalInterface
    private interface InventoryAction {
        ReservationResponse execute();
    }
}

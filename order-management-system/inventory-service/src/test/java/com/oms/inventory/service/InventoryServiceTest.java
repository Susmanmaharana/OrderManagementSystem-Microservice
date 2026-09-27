package com.oms.inventory.service;

import com.oms.inventory.config.InventoryProperties;
import com.oms.inventory.dto.ReservationResponse;
import com.oms.inventory.dto.ReserveRequest;
import com.oms.inventory.exception.ConcurrentInventoryUpdateException;
import com.oms.inventory.exception.InsufficientInventoryException;
import com.oms.inventory.exception.ResourceNotFoundException;
import com.oms.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryCommandService commandService;

    @Mock
    private InventoryProperties properties;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getOptimisticLockMaxRetries()).thenReturn(3);
        inventoryService = new InventoryService(inventoryRepository, commandService, properties);
    }

    @Test
    void reserve_retriesOnOptimisticLockThenSucceeds() {
        ReserveRequest request = request(1L, 101L, 2);
        ReservationResponse ok = new ReservationResponse();
        ok.setStatus("RESERVED");
        ok.setAvailableQuantity(48);

        when(commandService.reserve(any(ReserveRequest.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenReturn(ok);

        ReservationResponse response = inventoryService.reserve(request);

        assertEquals("RESERVED", response.getStatus());
        verify(commandService, times(2)).reserve(any(ReserveRequest.class));
    }

    @Test
    void reserve_exhaustedRetries_throwsConcurrent() {
        when(commandService.reserve(any(ReserveRequest.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(ConcurrentInventoryUpdateException.class,
                () -> inventoryService.reserve(request(1L, 101L, 2)));
        verify(commandService, times(3)).reserve(any(ReserveRequest.class));
    }

    @Test
    void reserve_propagatesInsufficientInventory() {
        when(commandService.reserve(any(ReserveRequest.class)))
                .thenThrow(new InsufficientInventoryException("not enough"));

        assertThrows(InsufficientInventoryException.class,
                () -> inventoryService.reserve(request(1L, 101L, 999)));
    }

    @Test
    void getInventory_whenMissing_throwsNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventory(999L));
    }

    private ReserveRequest request(Long orderId, Long productId, int quantity) {
        ReserveRequest request = new ReserveRequest();
        request.setOrderId(orderId);
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }
}

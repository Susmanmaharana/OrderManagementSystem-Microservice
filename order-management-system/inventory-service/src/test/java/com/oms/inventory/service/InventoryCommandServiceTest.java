package com.oms.inventory.service;

import com.oms.inventory.dto.ReservationResponse;
import com.oms.inventory.dto.ReserveRequest;
import com.oms.inventory.entity.Inventory;
import com.oms.inventory.entity.InventoryReservation;
import com.oms.inventory.entity.ReservationStatus;
import com.oms.inventory.exception.InsufficientInventoryException;
import com.oms.inventory.repository.InventoryRepository;
import com.oms.inventory.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    @InjectMocks
    private InventoryCommandService commandService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(50);
        inventory.setVersion(0L);
    }

    @Test
    void reserve_deductsStockAndCreatesReservation() {
        when(reservationRepository.findByOrderIdAndProductId(1L, 101L)).thenReturn(Optional.empty());
        when(inventoryRepository.findById(101L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = commandService.reserve(request(1L, 101L, 5));

        assertEquals("RESERVED", response.getStatus());
        assertEquals(45, response.getAvailableQuantity().intValue());
        assertEquals(45, inventory.getAvailableQuantity().intValue());
    }

    @Test
    void reserve_whenInsufficient_throws() {
        inventory.setAvailableQuantity(2);
        when(reservationRepository.findByOrderIdAndProductId(1L, 101L)).thenReturn(Optional.empty());
        when(inventoryRepository.findById(101L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientInventoryException.class,
                () -> commandService.reserve(request(1L, 101L, 5)));
        verify(reservationRepository, never()).save(any(InventoryReservation.class));
    }

    @Test
    void reserve_idempotentWhenAlreadyReserved() {
        InventoryReservation existing = new InventoryReservation();
        existing.setOrderId(1L);
        existing.setProductId(101L);
        existing.setQuantity(5);
        existing.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findByOrderIdAndProductId(1L, 101L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findById(101L)).thenReturn(Optional.of(inventory));

        ReservationResponse response = commandService.reserve(request(1L, 101L, 5));

        assertEquals("RESERVED", response.getStatus());
        assertEquals(50, response.getAvailableQuantity().intValue());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void release_restoresStock() {
        InventoryReservation existing = new InventoryReservation();
        existing.setOrderId(1L);
        existing.setProductId(101L);
        existing.setQuantity(5);
        existing.setStatus(ReservationStatus.RESERVED);
        inventory.setAvailableQuantity(45);

        when(reservationRepository.findByOrderIdAndProductId(1L, 101L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findById(101L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = commandService.release(request(1L, 101L, 5));

        assertEquals("RELEASED", response.getStatus());
        assertEquals(50, inventory.getAvailableQuantity().intValue());

        ArgumentCaptor<InventoryReservation> captor = ArgumentCaptor.forClass(InventoryReservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals(ReservationStatus.RELEASED, captor.getValue().getStatus());
    }

    @Test
    void release_idempotentWhenAlreadyReleased() {
        InventoryReservation existing = new InventoryReservation();
        existing.setOrderId(1L);
        existing.setProductId(101L);
        existing.setQuantity(5);
        existing.setStatus(ReservationStatus.RELEASED);

        when(reservationRepository.findByOrderIdAndProductId(1L, 101L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findById(101L)).thenReturn(Optional.of(inventory));

        ReservationResponse response = commandService.release(request(1L, 101L, 5));

        assertEquals("RELEASED", response.getStatus());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    private ReserveRequest request(Long orderId, Long productId, int quantity) {
        ReserveRequest request = new ReserveRequest();
        request.setOrderId(orderId);
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }
}

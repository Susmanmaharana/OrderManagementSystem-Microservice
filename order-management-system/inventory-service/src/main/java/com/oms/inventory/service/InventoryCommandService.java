package com.oms.inventory.service;

import com.oms.inventory.dto.ReservationResponse;
import com.oms.inventory.dto.ReserveRequest;
import com.oms.inventory.entity.Inventory;
import com.oms.inventory.entity.InventoryReservation;
import com.oms.inventory.entity.ReservationStatus;
import com.oms.inventory.exception.InsufficientInventoryException;
import com.oms.inventory.exception.ResourceNotFoundException;
import com.oms.inventory.repository.InventoryRepository;
import com.oms.inventory.repository.InventoryReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional stock mutations. Kept separate so optimistic-lock retries
 * create a new transaction per attempt (proxy call).
 */
@Service
public class InventoryCommandService {

    private static final Logger log = LoggerFactory.getLogger(InventoryCommandService.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryCommandService(InventoryRepository inventoryRepository,
                                   InventoryReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        InventoryReservation existing = reservationRepository
                .findByOrderIdAndProductId(request.getOrderId(), request.getProductId())
                .orElse(null);

        if (existing != null && existing.getStatus() == ReservationStatus.RESERVED) {
            Inventory inventory = findInventoryOrThrow(request.getProductId());
            log.info("Idempotent reserve orderId={} productId={}", request.getOrderId(), request.getProductId());
            return toReservationResponse(existing, inventory.getAvailableQuantity());
        }

        if (existing != null && existing.getStatus() == ReservationStatus.RELEASED) {
            Inventory inventory = findInventoryOrThrow(request.getProductId());
            deductStock(inventory, request.getQuantity());
            existing.setQuantity(request.getQuantity());
            existing.setStatus(ReservationStatus.RESERVED);
            reservationRepository.save(existing);
            log.info("Re-reserved orderId={} productId={} qty={}",
                    request.getOrderId(), request.getProductId(), request.getQuantity());
            return toReservationResponse(existing, inventory.getAvailableQuantity());
        }

        Inventory inventory = findInventoryOrThrow(request.getProductId());
        deductStock(inventory, request.getQuantity());

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(request.getOrderId());
        reservation.setProductId(request.getProductId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        log.info("Reserved orderId={} productId={} qty={} remaining={}",
                request.getOrderId(), request.getProductId(), request.getQuantity(), inventory.getAvailableQuantity());
        return toReservationResponse(reservation, inventory.getAvailableQuantity());
    }

    @Transactional
    public ReservationResponse release(ReserveRequest request) {
        InventoryReservation reservation = reservationRepository
                .findByOrderIdAndProductId(request.getOrderId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found for orderId=" + request.getOrderId()
                                + ", productId=" + request.getProductId()));

        Inventory inventory = findInventoryOrThrow(request.getProductId());

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            log.info("Idempotent release orderId={} productId={}", request.getOrderId(), request.getProductId());
            return toReservationResponse(reservation, inventory.getAvailableQuantity());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        log.info("Released orderId={} productId={} qty={} remaining={}",
                request.getOrderId(), request.getProductId(), reservation.getQuantity(), inventory.getAvailableQuantity());
        return toReservationResponse(reservation, inventory.getAvailableQuantity());
    }

    private void deductStock(Inventory inventory, Integer quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for productId=" + inventory.getProductId()
                            + ", available=" + inventory.getAvailableQuantity()
                            + ", requested=" + quantity);
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    private Inventory findInventoryOrThrow(Long productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId));
    }

    private ReservationResponse toReservationResponse(InventoryReservation reservation, Integer availableQuantity) {
        ReservationResponse response = new ReservationResponse();
        response.setOrderId(reservation.getOrderId());
        response.setProductId(reservation.getProductId());
        response.setQuantity(reservation.getQuantity());
        response.setStatus(reservation.getStatus().name());
        response.setAvailableQuantity(availableQuantity);
        return response;
    }
}

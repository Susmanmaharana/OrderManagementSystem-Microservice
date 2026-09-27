package com.oms.inventory.repository;

import com.oms.inventory.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);
}

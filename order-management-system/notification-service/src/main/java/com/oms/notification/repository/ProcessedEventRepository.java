package com.oms.notification.repository;

import com.oms.notification.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    List<ProcessedEvent> findAllByOrderByProcessedAtDesc();

    List<ProcessedEvent> findByOrderIdOrderByProcessedAtDesc(Long orderId);
}

package com.oms.notification.service;

import com.oms.notification.event.DomainEvent;
import com.oms.notification.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private NotificationService notificationService;

    private DomainEvent event;

    @BeforeEach
    void setUp() {
        event = new DomainEvent();
        event.setEventId("evt-1");
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderId(1001L);
        event.setCustomerId(2001L);
        event.setPayload(Collections.singletonMap("status", "CONFIRMED"));
    }

    @Test
    void handle_firstTime_savesProcessedEvent() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);

        notificationService.handle(event);

        ArgumentCaptor<com.oms.notification.entity.ProcessedEvent> captor =
                ArgumentCaptor.forClass(com.oms.notification.entity.ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertEquals("evt-1", captor.getValue().getEventId());
        assertEquals("ORDER_CONFIRMED", captor.getValue().getEventType());
        assertEquals(Long.valueOf(1001L), captor.getValue().getOrderId());
        assertEquals(Long.valueOf(2001L), captor.getValue().getCustomerId());
    }

    @Test
    void handle_duplicate_skipsSave() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(true);

        notificationService.handle(event);

        verify(processedEventRepository, never()).save(any(com.oms.notification.entity.ProcessedEvent.class));
    }

    @Test
    void handle_nullEventId_ignored() {
        event.setEventId(null);
        notificationService.handle(event);
        verify(processedEventRepository, never()).existsById(any(String.class));
        verify(processedEventRepository, never()).save(any(com.oms.notification.entity.ProcessedEvent.class));
    }

    @Test
    void handle_processesTwiceWithDifferentIds() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);
        when(processedEventRepository.existsById("evt-2")).thenReturn(false);

        notificationService.handle(event);
        event.setEventId("evt-2");
        notificationService.handle(event);

        verify(processedEventRepository, times(2)).save(any(com.oms.notification.entity.ProcessedEvent.class));
    }
}

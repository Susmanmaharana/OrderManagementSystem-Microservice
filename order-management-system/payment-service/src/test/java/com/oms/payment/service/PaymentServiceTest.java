package com.oms.payment.service;

import com.oms.payment.config.PaymentProperties;
import com.oms.payment.dto.CreatePaymentRequest;
import com.oms.payment.dto.PaymentResponse;
import com.oms.payment.entity.Payment;
import com.oms.payment.entity.PaymentStatus;
import com.oms.payment.exception.ResourceNotFoundException;
import com.oms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProperties properties;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getFailureRate()).thenReturn(0.0);
        lenient().when(properties.getDelayMs()).thenReturn(0L);
    }

    @Test
    void processPayment_success_assignsPaymentRef() {
        CreatePaymentRequest request = request(1001L, "2500.00", "key-1");
        stubSaveWithIds();

        PaymentResponse response = paymentService.processPayment(request, false);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("PAY-00001", response.getPaymentId());
        assertEquals(1001L, response.getOrderId().longValue());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(captor.capture());
        assertEquals(PaymentStatus.SUCCESS, captor.getAllValues().get(1).getStatus());
    }

    @Test
    void processPayment_forceFailure_returnsFailed() {
        CreatePaymentRequest request = request(1001L, "100.00", null);
        stubSaveWithIds();

        PaymentResponse response = paymentService.processPayment(request, true);

        assertEquals("FAILED", response.getStatus());
    }

    @Test
    void processPayment_alwaysFailRate_returnsFailed() {
        when(properties.getFailureRate()).thenReturn(1.0);
        CreatePaymentRequest request = request(1001L, "100.00", null);
        stubSaveWithIds();

        PaymentResponse response = paymentService.processPayment(request, false);

        assertEquals("FAILED", response.getStatus());
    }

    @Test
    void processPayment_idempotent_returnsExisting() {
        Payment existing = new Payment();
        existing.setId(9L);
        existing.setPaymentRef("PAY-00009");
        existing.setOrderId(1001L);
        existing.setAmount(new BigDecimal("100.00"));
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.setIdempotencyKey("key-1");
        existing.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.processPayment(request(1001L, "100.00", "key-1"), false);

        assertEquals("PAY-00009", response.getPaymentId());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getByPaymentRef_whenMissing_throwsNotFound() {
        when(paymentRepository.findByPaymentRef("PAY-99999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getByPaymentRef("PAY-99999"));
    }

    private CreatePaymentRequest request(Long orderId, String amount, String key) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal(amount));
        request.setIdempotencyKey(key);
        return request;
    }

    private void stubSaveWithIds() {
        final AtomicLong seq = new AtomicLong(1L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(seq.getAndIncrement());
                payment.setCreatedAt(LocalDateTime.now());
            }
            return payment;
        });
    }
}

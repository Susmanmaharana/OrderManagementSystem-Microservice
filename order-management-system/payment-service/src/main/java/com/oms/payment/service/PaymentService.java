package com.oms.payment.service;

import com.oms.payment.config.PaymentProperties;
import com.oms.payment.dto.CreatePaymentRequest;
import com.oms.payment.dto.PaymentResponse;
import com.oms.payment.entity.Payment;
import com.oms.payment.entity.PaymentStatus;
import com.oms.payment.exception.ResourceNotFoundException;
import com.oms.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProperties properties;

    public PaymentService(PaymentRepository paymentRepository, PaymentProperties properties) {
        this.paymentRepository = paymentRepository;
        this.properties = properties;
    }

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request, boolean forceFailure) {
        String idempotencyKey = resolveIdempotencyKey(request);
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent payment hit key={} paymentRef={}",
                        idempotencyKey, existing.get().getPaymentRef());
                return toResponse(existing.get());
            }
        }

        applyConfiguredDelay();

        PaymentStatus status = shouldFail(forceFailure) ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(status);
        if (StringUtils.hasText(idempotencyKey)) {
            payment.setIdempotencyKey(idempotencyKey.trim());
        }
        // Temporary unique ref before id is assigned; replaced after save
        payment.setPaymentRef("TEMP-" + System.nanoTime());

        Payment saved = paymentRepository.save(payment);
        saved.setPaymentRef(formatPaymentRef(saved.getId()));
        Payment finalized = paymentRepository.save(saved);

        log.info("Processed paymentRef={} orderId={} status={}",
                finalized.getPaymentRef(), finalized.getOrderId(), finalized.getStatus());
        return toResponse(finalized);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByPaymentRef(String paymentRef) {
        Payment payment = paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentRef));
        return toResponse(payment);
    }

    private String resolveIdempotencyKey(CreatePaymentRequest request) {
        return request.getIdempotencyKey();
    }

    private boolean shouldFail(boolean forceFailure) {
        if (forceFailure) {
            return true;
        }
        double rate = properties.getFailureRate();
        if (rate <= 0.0) {
            return false;
        }
        if (rate >= 1.0) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < rate;
    }

    private void applyConfiguredDelay() {
        long delayMs = properties.getDelayMs();
        if (delayMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment delay interrupted", ex);
        }
    }

    private String formatPaymentRef(Long id) {
        return "PAY-" + String.format("%05d", id);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentRef());
        response.setOrderId(payment.getOrderId());
        response.setStatus(payment.getStatus().name());
        response.setAmount(payment.getAmount());
        return response;
    }
}

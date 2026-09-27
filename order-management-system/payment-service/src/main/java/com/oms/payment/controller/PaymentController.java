package com.oms.payment.controller;

import com.oms.payment.dto.CreatePaymentRequest;
import com.oms.payment.dto.PaymentResponse;
import com.oms.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Process simulated payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @RequestHeader(value = "X-Force-Payment-Failure", required = false) String forceFailureHeader) {

        if (!StringUtils.hasText(request.getIdempotencyKey()) && StringUtils.hasText(idempotencyHeader)) {
            request.setIdempotencyKey(idempotencyHeader.trim());
        }

        boolean forceFailure = "true".equalsIgnoreCase(forceFailureHeader);
        PaymentResponse response = paymentService.processPayment(request, forceFailure);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by payment id (e.g. PAY-00001)")
    public PaymentResponse getPayment(@PathVariable String paymentId) {
        return paymentService.getByPaymentRef(paymentId);
    }
}

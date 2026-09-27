package com.oms.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void processPayment_success() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":5001,\"amount\":250.00,\"idempotencyKey\":\"pay-it-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.paymentId", startsWith("PAY-")));
    }

    @Test
    void processPayment_forceFailure() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Force-Payment-Failure", "true")
                        .content("{\"orderId\":5002,\"amount\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    void processPayment_idempotent() throws Exception {
        MvcResult first = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "pay-dup-1")
                        .content("{\"orderId\":5003,\"amount\":50.00}"))
                .andExpect(status().isCreated())
                .andReturn();

        String paymentId = first.getResponse().getContentAsString()
                .replaceAll("(?s).*\"paymentId\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "pay-dup-1")
                        .content("{\"orderId\":5003,\"amount\":50.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId", is(paymentId)));

        mockMvc.perform(get("/api/v1/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(5003)));
    }

    @Test
    void processPayment_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":null,\"amount\":-1}"))
                .andExpect(status().isBadRequest());
    }
}

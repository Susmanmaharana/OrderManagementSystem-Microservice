package com.oms.order;

import com.oms.order.client.InventoryClient;
import com.oms.order.client.InventoryReservationResponse;
import com.oms.order.client.PaymentClient;
import com.oms.order.client.PaymentResponse;
import com.oms.order.exception.InsufficientInventoryException;
import com.oms.order.exception.PaymentServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private PaymentClient paymentClient;

    @BeforeEach
    void resetMocks() {
        reset(inventoryClient, paymentClient);
    }

    @Test
    void createOrder_happyPath_returnsConfirmed() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(2))).thenReturn(reserved());
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), eq(false)))
                .thenReturn(payment("SUCCESS", "PAY-IT-1"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.customerId", is(1001)))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void createOrder_insufficientInventory_returnsConflict() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(99)))
                .thenThrow(new InsufficientInventoryException("Insufficient inventory"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":99}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        verify(paymentClient, never()).pay(anyLong(), any(BigDecimal.class), anyString(), anyBoolean());
    }

    @Test
    void createOrder_paymentFailed_returnsPaymentFailedAndReleases() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(1))).thenReturn(reserved());
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), eq(false)))
                .thenReturn(payment("FAILED", "PAY-IT-2"));
        when(inventoryClient.release(anyLong(), eq(101L), eq(1))).thenReturn(released());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PAYMENT_FAILED")));

        verify(inventoryClient).release(anyLong(), eq(101L), eq(1));
    }

    @Test
    void createOrder_paymentDown_returnsServiceUnavailableAndReleases() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(1))).thenReturn(reserved());
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), eq(false)))
                .thenThrow(new PaymentServiceException("down"));
        when(inventoryClient.release(anyLong(), eq(101L), eq(1))).thenReturn(released());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":1}]}"))
                .andExpect(status().isServiceUnavailable());

        verify(inventoryClient).release(anyLong(), eq(101L), eq(1));
    }

    @Test
    void createOrder_idempotentKey_returnsSameOrder() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(1))).thenReturn(reserved());
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), eq(false)))
                .thenReturn(payment("SUCCESS", "PAY-IT-3"));

        MvcResult first = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "it-key-1")
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andReturn();

        String body = first.getResponse().getContentAsString();
        String orderId = body.replaceAll("(?s).*\"orderId\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "it-key-1")
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(Integer.parseInt(orderId))));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/orders/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_validationError_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":null,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrder_afterCreateFailurePath_fromConfirmed_conflicts() throws Exception {
        when(inventoryClient.reserve(anyLong(), eq(101L), eq(1))).thenReturn(reserved());
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), eq(false)))
                .thenReturn(payment("SUCCESS", "PAY-IT-4"));

        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1001,\"items\":[{\"productId\":101,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andReturn();

        String orderId = created.getResponse().getContentAsString()
                .replaceAll("(?s).*\"orderId\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/cancel"))
                .andExpect(status().isConflict());
    }

    private InventoryReservationResponse reserved() {
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setStatus("RESERVED");
        response.setAvailableQuantity(48);
        return response;
    }

    private InventoryReservationResponse released() {
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setStatus("RELEASED");
        response.setAvailableQuantity(50);
        return response;
    }

    private PaymentResponse payment(String status, String paymentId) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus(status);
        response.setPaymentId(paymentId);
        return response;
    }
}

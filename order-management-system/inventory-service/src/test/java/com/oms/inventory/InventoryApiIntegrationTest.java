package com.oms.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSeededInventory() throws Exception {
        mockMvc.perform(put("/api/v1/inventory/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableQuantity\":50}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/inventory/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(101)))
                .andExpect(jsonPath("$.availableQuantity", is(50)));
    }

    @Test
    void reserveAndRelease_idempotent() throws Exception {
        mockMvc.perform(put("/api/v1/inventory/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableQuantity\":50}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":9001,\"productId\":101,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESERVED")))
                .andExpect(jsonPath("$.availableQuantity", lessThan(50)));

        // idempotent second reserve
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":9001,\"productId\":101,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESERVED")));

        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":9001,\"productId\":101,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RELEASED")));

        // idempotent second release
        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":9001,\"productId\":101,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RELEASED")));
    }

    @Test
    void reserve_insufficient_returnsConflict() throws Exception {
        mockMvc.perform(put("/api/v1/inventory/102")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableQuantity\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":9002,\"productId\":102,\"quantity\":10}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getMissingProduct_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/99999"))
                .andExpect(status().isNotFound());
    }
}

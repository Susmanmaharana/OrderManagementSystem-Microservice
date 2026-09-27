package com.oms.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationIdGlobalFilterTest {

    private final CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();

    @Test
    void generatesCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders/1").build());

        AtomicReference<String> downstreamHeader = new AtomicReference<String>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(downstreamHeader.get());
        assertFalse(downstreamHeader.get().trim().isEmpty());
        assertEquals(downstreamHeader.get(),
                exchange.getResponse().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
    }

    @Test
    void preservesIncomingCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders/1")
                        .header(CorrelationIdGlobalFilter.HEADER, "corr-fixed-123")
                        .build());

        AtomicReference<String> downstreamHeader = new AtomicReference<String>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertEquals("corr-fixed-123", downstreamHeader.get());
        assertEquals("corr-fixed-123",
                exchange.getResponse().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
    }

    @Test
    void orderIsBeforeLoggingFilter() {
        assertTrue(filter.getOrder() < -90);
    }
}

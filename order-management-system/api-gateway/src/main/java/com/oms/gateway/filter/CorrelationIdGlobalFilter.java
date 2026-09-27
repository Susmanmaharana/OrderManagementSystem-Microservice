package com.oms.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ensures every request has X-Correlation-Id and propagates it downstream.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String cid = correlationId;
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(HEADER, cid)
                .build();
        exchange.getResponse().getHeaders().set(HEADER, cid);

        ServerWebExchange mutated = exchange.mutate().request(request).build();
        MDC.put(MDC_KEY, cid);

        return chain.filter(mutated)
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

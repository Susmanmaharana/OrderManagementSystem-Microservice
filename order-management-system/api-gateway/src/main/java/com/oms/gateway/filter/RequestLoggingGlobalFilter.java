package com.oms.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Lightweight request logging for interview demos / troubleshooting.
 */
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getURI().getRawPath();
        String correlationId = request.getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER);

        log.info("Gateway request method={} path={} correlationId={}", method, path, correlationId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;
            log.info("Gateway response method={} path={} status={} durationMs={} correlationId={}",
                    method, path, status, System.currentTimeMillis() - start, correlationId);
        }));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}

package com.oms.order.config;

import com.oms.order.exception.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({
        OrderProperties.class,
        InventoryClientProperties.class,
        PaymentClientProperties.class
})
public class AppConfig {

    @Bean
    public RestTemplate inventoryRestTemplate(RestTemplateBuilder builder,
                                              InventoryClientProperties properties) {
        return buildRestTemplate(builder, properties.getConnectTimeoutMs(), properties.getReadTimeoutMs());
    }

    @Bean
    public RestTemplate paymentRestTemplate(RestTemplateBuilder builder,
                                            PaymentClientProperties properties) {
        return buildRestTemplate(builder, properties.getConnectTimeoutMs(), properties.getReadTimeoutMs());
    }

    private RestTemplate buildRestTemplate(RestTemplateBuilder builder, int connectTimeoutMs, int readTimeoutMs) {
        ClientHttpRequestInterceptor correlationInterceptor = (request, body, execution) -> {
            String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
            if (correlationId != null && !correlationId.isEmpty()) {
                request.getHeaders().set(CorrelationIdFilter.HEADER, correlationId);
            }
            return execution.execute(request, body);
        };

        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .additionalInterceptors(correlationInterceptor)
                .build();
    }
}

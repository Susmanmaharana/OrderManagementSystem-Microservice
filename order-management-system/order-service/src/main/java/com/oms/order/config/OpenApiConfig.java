package com.oms.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Phase 1: create, view, and cancel orders")
                        .version("v1"));
    }
}

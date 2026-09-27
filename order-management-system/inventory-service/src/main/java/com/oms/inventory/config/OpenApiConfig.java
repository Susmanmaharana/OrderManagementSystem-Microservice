package com.oms.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("Phase 2: check, reserve, and release stock with optimistic locking")
                        .version("v1"));
    }
}

package com.oms.inventory.config;

import com.oms.inventory.entity.Inventory;
import com.oms.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryDataLoader {

    private static final Logger log = LoggerFactory.getLogger(InventoryDataLoader.class);

    @Bean
    CommandLineRunner seedInventory(InventoryRepository inventoryRepository) {
        return args -> {
            seedIfMissing(inventoryRepository, 101L, 50);
            seedIfMissing(inventoryRepository, 102L, 30);
            seedIfMissing(inventoryRepository, 103L, 10);
            log.info("Inventory seed complete");
        };
    }

    private void seedIfMissing(InventoryRepository repository, Long productId, int quantity) {
        if (!repository.existsById(productId)) {
            Inventory inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setAvailableQuantity(quantity);
            repository.save(inventory);
            log.info("Seeded productId={} quantity={}", productId, quantity);
        }
    }
}

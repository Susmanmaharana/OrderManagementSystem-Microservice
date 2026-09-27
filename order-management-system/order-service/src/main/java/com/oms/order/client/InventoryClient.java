package com.oms.order.client;

import com.oms.order.config.InventoryClientProperties;
import com.oms.order.exception.InsufficientInventoryException;
import com.oms.order.exception.InventoryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestTemplate restTemplate;
    private final InventoryClientProperties properties;

    public InventoryClient(RestTemplate inventoryRestTemplate, InventoryClientProperties properties) {
        this.restTemplate = inventoryRestTemplate;
        this.properties = properties;
    }

    public InventoryReservationResponse reserve(Long orderId, Long productId, Integer quantity) {
        return post("/api/v1/inventory/reserve", orderId, productId, quantity);
    }

    public InventoryReservationResponse release(Long orderId, Long productId, Integer quantity) {
        return post("/api/v1/inventory/release", orderId, productId, quantity);
    }

    private InventoryReservationResponse post(String path, Long orderId, Long productId, Integer quantity) {
        String url = properties.getBaseUrl() + path;
        InventoryReserveRequest body = new InventoryReserveRequest(orderId, productId, quantity);
        try {
            log.info("Calling inventory {} orderId={} productId={} qty={}", path, orderId, productId, quantity);
            ResponseEntity<InventoryReservationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<InventoryReserveRequest>(body),
                    InventoryReservationResponse.class);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            if (ex.getStatusCode().value() == 404) {
                throw new InsufficientInventoryException(
                        "Product not found in inventory: productId=" + productId);
            }
            if (ex.getStatusCode().value() == 409) {
                throw new InsufficientInventoryException(
                        extractMessage(responseBody, "Insufficient inventory for productId=" + productId));
            }
            throw new InventoryServiceException(
                    "Inventory service error " + ex.getStatusCode().value() + " on " + path + ": "
                            + extractMessage(responseBody, ex.getMessage()));
        } catch (ResourceAccessException ex) {
            throw new InventoryServiceException("Inventory service unavailable: " + ex.getMessage(), ex);
        }
    }

    private String extractMessage(String responseBody, String fallback) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return fallback;
        }
        // Prefer JSON "message" field when present
        int keyIndex = responseBody.indexOf("\"message\"");
        if (keyIndex >= 0) {
            int colon = responseBody.indexOf(':', keyIndex);
            int firstQuote = responseBody.indexOf('"', colon + 1);
            int secondQuote = responseBody.indexOf('"', firstQuote + 1);
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                return responseBody.substring(firstQuote + 1, secondQuote);
            }
        }
        return responseBody;
    }
}

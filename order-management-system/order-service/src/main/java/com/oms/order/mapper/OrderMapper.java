package com.oms.order.mapper;

import com.oms.order.dto.OrderItemResponse;
import com.oms.order.dto.OrderResponse;
import com.oms.order.entity.Order;
import com.oms.order.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(mapItems(order.getItems()));
        return response;
    }

    private static List<OrderItemResponse> mapItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getPrice()))
                .collect(Collectors.toList());
    }
}

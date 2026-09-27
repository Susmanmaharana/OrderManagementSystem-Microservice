package com.oms.order.service;

import com.oms.order.client.InventoryClient;
import com.oms.order.client.PaymentClient;
import com.oms.order.client.PaymentResponse;
import com.oms.order.config.OrderProperties;
import com.oms.order.dto.CreateOrderRequest;
import com.oms.order.dto.OrderItemRequest;
import com.oms.order.dto.OrderResponse;
import com.oms.order.entity.Order;
import com.oms.order.entity.OrderItem;
import com.oms.order.entity.OrderStatus;
import com.oms.order.event.EventTypes;
import com.oms.order.event.OrderEventPublisher;
import com.oms.order.exception.InvalidOrderStateException;
import com.oms.order.exception.PaymentServiceException;
import com.oms.order.exception.ResourceNotFoundException;
import com.oms.order.mapper.OrderMapper;
import com.oms.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderProperties orderProperties;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        OrderProperties orderProperties,
                        InventoryClient inventoryClient,
                        PaymentClient paymentClient,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderProperties = orderProperties;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey, boolean forcePaymentFailure) {
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent hit for key={}, returning orderId={}", idempotencyKey, existing.get().getId());
                return OrderMapper.toResponse(existing.get());
            }
        }

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        if (StringUtils.hasText(idempotencyKey)) {
            order.setIdempotencyKey(idempotencyKey.trim());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(orderProperties.getDefaultUnitPrice());
            order.addItem(item);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity().longValue())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Created orderId={} customerId={} status={}", saved.getId(), saved.getCustomerId(), saved.getStatus());

        // Saga step 1: reserve inventory
        reserveInventoryOrCompensate(saved);
        saved.setStatus(OrderStatus.INVENTORY_RESERVED);
        saved = orderRepository.save(saved);
        log.info("Inventory reserved for orderId={}", saved.getId());
        eventPublisher.publishOrderEvent(EventTypes.ORDER_CREATED, saved);

        // Saga step 2: payment
        saved.setStatus(OrderStatus.PAYMENT_PENDING);
        saved = orderRepository.save(saved);

        return processPaymentStep(saved, forcePaymentFailure);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.CREATED
                && order.getStatus() != OrderStatus.INVENTORY_RESERVED
                && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException(
                    "Order " + orderId + " cannot be cancelled from status " + order.getStatus());
        }

        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED
                || order.getStatus() == OrderStatus.PAYMENT_PENDING) {
            releaseAllItems(order);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Cancelled orderId={}", saved.getId());
        eventPublisher.publishOrderEvent(EventTypes.ORDER_CANCELLED, saved);
        return OrderMapper.toResponse(saved);
    }

    private OrderResponse processPaymentStep(Order order, boolean forcePaymentFailure) {
        String paymentKey = StringUtils.hasText(order.getIdempotencyKey())
                ? "order-" + order.getIdempotencyKey()
                : "order-" + order.getId();

        try {
            PaymentResponse payment = paymentClient.pay(
                    order.getId(), order.getTotalAmount(), paymentKey, forcePaymentFailure);

            if (payment != null && "SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                order.setStatus(OrderStatus.CONFIRMED);
                Order confirmed = orderRepository.save(order);
                log.info("Order confirmed orderId={} paymentId={}", confirmed.getId(), payment.getPaymentId());
                eventPublisher.publishOrderEvent(EventTypes.ORDER_CONFIRMED, confirmed);
                eventPublisher.publishPaymentEvent(EventTypes.PAYMENT_COMPLETED, confirmed, payment.getPaymentId());
                return OrderMapper.toResponse(confirmed);
            }

            log.warn("Payment failed for orderId={}, releasing inventory", order.getId());
            releaseAllItemsSafely(order);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            Order failed = orderRepository.save(order);
            String paymentId = payment != null ? payment.getPaymentId() : null;
            eventPublisher.publishPaymentEvent(EventTypes.PAYMENT_FAILED, failed, paymentId);
            return OrderMapper.toResponse(failed);
        } catch (PaymentServiceException ex) {
            log.error("Payment service error for orderId={}, compensating inventory", order.getId(), ex);
            releaseAllItemsSafely(order);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            Order failed = orderRepository.save(order);
            eventPublisher.publishPaymentEvent(EventTypes.PAYMENT_FAILED, failed, null);
            throw ex;
        }
    }

    private void reserveInventoryOrCompensate(Order order) {
        List<OrderItem> reservedItems = new ArrayList<OrderItem>();
        try {
            for (OrderItem item : order.getItems()) {
                inventoryClient.reserve(order.getId(), item.getProductId(), item.getQuantity());
                reservedItems.add(item);
            }
        } catch (RuntimeException ex) {
            log.warn("Inventory reserve failed for orderId={}, compensating {} item(s)",
                    order.getId(), reservedItems.size());
            for (OrderItem item : reservedItems) {
                try {
                    inventoryClient.release(order.getId(), item.getProductId(), item.getQuantity());
                } catch (RuntimeException releaseEx) {
                    log.error("Failed to release inventory during compensation orderId={} productId={}",
                            order.getId(), item.getProductId(), releaseEx);
                }
            }
            throw ex;
        }
    }

    private void releaseAllItems(Order order) {
        for (OrderItem item : order.getItems()) {
            inventoryClient.release(order.getId(), item.getProductId(), item.getQuantity());
        }
    }

    private void releaseAllItemsSafely(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.release(order.getId(), item.getProductId(), item.getQuantity());
            } catch (RuntimeException ex) {
                log.error("Failed to release inventory orderId={} productId={}",
                        order.getId(), item.getProductId(), ex);
            }
        }
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}

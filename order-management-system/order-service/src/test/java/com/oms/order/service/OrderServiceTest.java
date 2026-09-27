package com.oms.order.service;

import com.oms.order.client.InventoryClient;
import com.oms.order.client.InventoryReservationResponse;
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
import com.oms.order.exception.InsufficientInventoryException;
import com.oms.order.exception.InvalidOrderStateException;
import com.oms.order.exception.PaymentServiceException;
import com.oms.order.exception.ResourceNotFoundException;
import com.oms.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProperties orderProperties;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        lenient().when(orderProperties.getDefaultUnitPrice()).thenReturn(new BigDecimal("100.00"));
    }

    @Test
    void createOrder_happyPath_confirmsAfterPayment() {
        CreateOrderRequest request = buildRequest(1001L, 101L, 2);
        stubOrderSave();
        when(inventoryClient.reserve(eq(1L), eq(101L), eq(2))).thenReturn(reservation("RESERVED", 48));
        when(paymentClient.pay(eq(1L), eq(new BigDecimal("200.00")), eq("order-1"), eq(false)))
                .thenReturn(payment("PAY-00001", "SUCCESS"));

        OrderResponse response = orderService.createOrder(request, null, false);

        assertEquals("CONFIRMED", response.getStatus());
        verify(inventoryClient).reserve(1L, 101L, 2);
        verify(paymentClient).pay(eq(1L), eq(new BigDecimal("200.00")), eq("order-1"), eq(false));
        verify(inventoryClient, never()).release(anyLong(), anyLong(), anyInt());
        verify(eventPublisher).publishOrderEvent(eq(EventTypes.ORDER_CREATED), any(Order.class));
        verify(eventPublisher).publishOrderEvent(eq(EventTypes.ORDER_CONFIRMED), any(Order.class));
        verify(eventPublisher).publishPaymentEvent(eq(EventTypes.PAYMENT_COMPLETED), any(Order.class), eq("PAY-00001"));
    }

    @Test
    void createOrder_paymentFailed_releasesInventory() {
        CreateOrderRequest request = buildRequest(1001L, 101L, 2);
        stubOrderSave();
        when(inventoryClient.reserve(eq(1L), eq(101L), eq(2))).thenReturn(reservation("RESERVED", 48));
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), anyBoolean()))
                .thenReturn(payment("PAY-00002", "FAILED"));
        when(inventoryClient.release(eq(1L), eq(101L), eq(2))).thenReturn(reservation("RELEASED", 50));

        OrderResponse response = orderService.createOrder(request, null, false);

        assertEquals("PAYMENT_FAILED", response.getStatus());
        verify(inventoryClient).release(1L, 101L, 2);
        verify(eventPublisher).publishPaymentEvent(eq(EventTypes.PAYMENT_FAILED), any(Order.class), eq("PAY-00002"));
    }

    @Test
    void createOrder_paymentUnavailable_compensatesAndThrows() {
        CreateOrderRequest request = buildRequest(1001L, 101L, 2);
        stubOrderSave();
        when(inventoryClient.reserve(eq(1L), eq(101L), eq(2))).thenReturn(reservation("RESERVED", 48));
        when(paymentClient.pay(anyLong(), any(BigDecimal.class), anyString(), anyBoolean()))
                .thenThrow(new PaymentServiceException("down"));
        when(inventoryClient.release(eq(1L), eq(101L), eq(2))).thenReturn(reservation("RELEASED", 50));

        assertThrows(PaymentServiceException.class, () -> orderService.createOrder(request, null, false));
        verify(inventoryClient).release(1L, 101L, 2);
    }

    @Test
    void createOrder_whenSecondItemFails_releasesFirstAndRethrows() {
        OrderItemRequest item1 = item(101L, 1);
        OrderItemRequest item2 = item(102L, 1);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1001L);
        request.setItems(Arrays.asList(item1, item2));

        stubOrderSave();
        when(inventoryClient.reserve(eq(1L), eq(101L), eq(1))).thenReturn(reservation("RESERVED", 49));
        when(inventoryClient.reserve(eq(1L), eq(102L), eq(1)))
                .thenThrow(new InsufficientInventoryException("Insufficient inventory"));

        assertThrows(InsufficientInventoryException.class, () -> orderService.createOrder(request, null, false));
        verify(inventoryClient).release(1L, 101L, 1);
        verify(paymentClient, never()).pay(anyLong(), any(BigDecimal.class), anyString(), anyBoolean());
    }

    @Test
    void createOrder_withSameIdempotencyKey_skipsDownstream() {
        Order existing = new Order();
        existing.setId(9L);
        existing.setCustomerId(1001L);
        existing.setStatus(OrderStatus.CONFIRMED);
        existing.setTotalAmount(new BigDecimal("100.00"));
        existing.setCreatedAt(LocalDateTime.now());
        existing.setIdempotencyKey("key-1");

        when(orderRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        OrderResponse response = orderService.createOrder(buildRequest(1001L, 101L, 1), "key-1", false);

        assertEquals(9L, response.getOrderId());
        verify(inventoryClient, never()).reserve(anyLong(), anyLong(), anyInt());
        verify(paymentClient, never()).pay(anyLong(), any(BigDecimal.class), anyString(), anyBoolean());
    }

    @Test
    void getOrder_whenMissing_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(99L));
    }

    @Test
    void cancelOrder_fromInventoryReserved_releasesStock() {
        Order order = orderWithItem(5L, OrderStatus.INVENTORY_RESERVED, 101L, 2);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryClient.release(eq(5L), eq(101L), eq(2))).thenReturn(reservation("RELEASED", 50));

        OrderResponse response = orderService.cancelOrder(5L);

        assertEquals("CANCELLED", response.getStatus());
        verify(inventoryClient).release(5L, 101L, 2);
        verify(eventPublisher).publishOrderEvent(eq(EventTypes.ORDER_CANCELLED), any(Order.class));
    }

    @Test
    void cancelOrder_fromPaymentPending_releasesStock() {
        Order order = orderWithItem(5L, OrderStatus.PAYMENT_PENDING, 101L, 2);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryClient.release(eq(5L), eq(101L), eq(2))).thenReturn(reservation("RELEASED", 50));

        assertEquals("CANCELLED", orderService.cancelOrder(5L).getStatus());
        verify(inventoryClient).release(5L, 101L, 2);
    }

    @Test
    void cancelOrder_fromCreated_skipsRelease() {
        Order order = new Order();
        order.setId(5L);
        order.setCustomerId(1001L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("CANCELLED", orderService.cancelOrder(5L).getStatus());
        verify(inventoryClient, never()).release(anyLong(), anyLong(), anyInt());
    }

    @Test
    void cancelOrder_fromConfirmed_throwsConflict() {
        Order order = new Order();
        order.setId(5L);
        order.setCustomerId(1001L);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(5L));
    }

    @Test
    void getOrdersByCustomer_mapsResults() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1001L);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(1001L))
                .thenReturn(Collections.singletonList(order));

        assertEquals(1, orderService.getOrdersByCustomer(1001L).size());
    }

    private void stubOrderSave() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(1L);
                order.setCreatedAt(LocalDateTime.now());
            }
            return order;
        });
    }

    private Order orderWithItem(Long id, OrderStatus status, Long productId, int qty) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerId(1001L);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(qty);
        item.setPrice(new BigDecimal("100.00"));
        order.addItem(item);
        return order;
    }

    private CreateOrderRequest buildRequest(Long customerId, Long productId, int quantity) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setItems(Collections.singletonList(item(productId, quantity)));
        return request;
    }

    private OrderItemRequest item(Long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private InventoryReservationResponse reservation(String status, int available) {
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setStatus(status);
        response.setAvailableQuantity(available);
        return response;
    }

    private PaymentResponse payment(String paymentId, String status) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setStatus(status);
        return response;
    }
}

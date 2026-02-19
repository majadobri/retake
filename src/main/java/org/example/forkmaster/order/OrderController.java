package org.example.forkmaster.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("GET /api/orders - fetching all orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/orders/customer/{}", customerId);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/unshipped")
    public ResponseEntity<List<OrderResponseDTO>> getUnshippedOrders() {
        log.info("GET /api/orders/unshipped");
        return ResponseEntity.ok(orderService.getUnshippedOrders());
    }

    @GetMapping("/shipped")
    public ResponseEntity<List<OrderResponseDTO>> getShippedOrders() {
        log.info("GET /api/orders/shipped");
        return ResponseEntity.ok(orderService.getShippedOrders());
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        log.info("POST /api/orders - creating order for customer: {}", request.getCustomerId());
        OrderResponseDTO createdOrder = orderService.createOrderForCustomer(request.getCustomerId(), request);
        log.info("Order created with id: {}", createdOrder.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<OrderResponseDTO> markAsShipped(@PathVariable Long id) {
        log.info("PUT /api/orders/{}/ship", id);
        return ResponseEntity.ok(orderService.markAsShipped(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("DELETE /api/orders/{}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-sold")
    public ResponseEntity<Integer> getTotalProductSoldBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String productName) {
        log.info("GET /api/orders/total-sold - product: {}, from: {}, to: {}", productName, startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        Integer total = orderService.getTotalProductSoldBetweenDates(start, end, productName);

        return ResponseEntity.ok(total);
    }
}

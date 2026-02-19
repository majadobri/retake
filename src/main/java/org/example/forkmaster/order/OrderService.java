package org.example.forkmaster.order;

import jakarta.transaction.Transactional;
import org.example.forkmaster.address.Address;
import org.example.forkmaster.address.AddressService;
import org.example.forkmaster.customer.Customer;
import org.example.forkmaster.customer.CustomerService;
import org.example.forkmaster.exception.OrderNotFoundException;
import org.example.forkmaster.exception.OutOfStockException;
import org.example.forkmaster.product.Product;
import org.example.forkmaster.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepo orderRepo;
    private final ProductService productService;
    private final AddressService addressService;
    private final CustomerService customerService;
    private final OrderLineRepo orderLineRepo;

    public OrderService(OrderRepo orderRepo, ProductService productService,
                        AddressService addressService, CustomerService customerService,
                        OrderLineRepo orderLineRepo) {
        this.orderRepo = orderRepo;
        this.productService = productService;
        this.addressService = addressService;
        this.customerService = customerService;
        this.orderLineRepo = orderLineRepo;
    }

    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        log.info("Found {} orders", orders.size());
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long id) {
        log.info("Finding order by id: {}", id);
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });
        return convertToDTO(order);
    }

    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        log.info("Finding orders for customer: {}", customerId);
        return orderRepo.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getUnshippedOrders() {
        List<Order> orders = orderRepo.findByShippedFalse();
        log.info("Found {} unshipped orders", orders.size());
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getShippedOrders() {
        List<Order> orders = orderRepo.findByShippedTrue();
        log.info("Found {} shipped orders", orders.size());
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO createOrderForCustomer(Long customerId, OrderRequestDTO request) {
        log.info("Creating order for customer: {}", customerId);

        Customer customer = customerService.findById(customerId);
        Address shippingAddress = addressService.findById(request.getShippingAddressId());

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setOrderStatus(request.getOrderStatus());
        order.setOrderType(request.getOrderType());
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setShippingCharge(request.getShippingCharge());

        BigDecimal totalPrice = request.getShippingCharge() != null ? request.getShippingCharge() : BigDecimal.ZERO;

        for (OrderRequestDTO.OrderLineRequest lineRequest : request.getOrderLines()) {
            Product product = productService.findById(lineRequest.getProductId());

            if (product.getQuantity() < lineRequest.getQuantity()) {
                log.warn("Out of stock: {}", product.getProductName());
                throw new OutOfStockException("Product out of stock: " + product.getProductName());
            }

            OrderLine orderLine = new OrderLine(product, lineRequest.getQuantity());
            order.addOrderLine(orderLine);
            totalPrice = totalPrice.add(orderLine.getLineTotal());

            productService.reduceQuantity(product.getId(), lineRequest.getQuantity());
        }

        order.setTotalPrice(totalPrice);
        Order saved = orderRepo.save(order);
        log.info("Order created with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public OrderResponseDTO markAsShipped(Long id) {
        log.info("Marking order {} as shipped", id);
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        order.setShipped(true);
        orderRepo.save(order);
        return convertToDTO(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);
        if (!orderRepo.existsById(id)) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }
        orderRepo.deleteById(id);
    }

    public Integer getTotalProductSoldBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, String productName) {
        log.info("Getting total {} sold between {} and {}", productName, startDate, endDate);
        Integer result = orderLineRepo.getTotalProductSoldBetweenDates(startDate, endDate, productName);
        return result != null ? result : 0;
    }

    private OrderResponseDTO convertToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                new OrderResponseDTO.AddressResponse(
                        order.getShippingAddress().getId(),
                        order.getShippingAddress().getStreet(),
                        order.getShippingAddress().getCity(),
                        order.getShippingAddress().getPostalCode(),
                        order.getShippingAddress().getCountry()
                ),
                order.getOrderLines().stream()
                        .map(line -> new OrderResponseDTO.OrderLineResponse(
                                line.getId(),
                                line.getProduct().getId(),
                                line.getProduct().getProductName(),
                                line.getQuantity(),
                                line.getPriceAtPurchase(),
                                line.getLineTotal()
                        ))
                        .collect(Collectors.toList()),
                order.getShippingCharge(),
                order.getTotalPrice(),
                order.isShipped(),
                order.getOrderStatus(),
                order.getOrderType(),
                order.getOrderDate()
        );
    }
}

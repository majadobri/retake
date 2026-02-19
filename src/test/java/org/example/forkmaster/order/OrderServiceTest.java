package org.example.forkmaster.order;

import org.example.forkmaster.address.Address;
import org.example.forkmaster.address.AddressService;
import org.example.forkmaster.customer.Customer;
import org.example.forkmaster.customer.CustomerService;
import org.example.forkmaster.exception.OrderNotFoundException;
import org.example.forkmaster.exception.OutOfStockException;
import org.example.forkmaster.product.Product;
import org.example.forkmaster.product.ProductService;
import org.example.forkmaster.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private ProductService productService;
    @Mock
    private AddressService addressService;
    @Mock
    private CustomerService customerService;
    @Mock
    private OrderLineRepo orderLineRepo;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Address testAddress;
    private Product testProduct;

    @BeforeEach
    void setup() {
        testCustomer = new Customer(1L, "Frank", "Sinatra", "frank@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setStreet("Storgata 1");
        testAddress.setCity("Oslo");
        testAddress.setPostalCode("0150");
        testAddress.setCountry("Norge");
        testAddress.setCustomer(testCustomer);

        testProduct = new Product(1L, "Teaspoon", "Knife", BigDecimal.valueOf(50), 10, ProductStatus.IN_STOCK);
    }

    @Test
    void getAllOrders_returnsListOfDTOs() {
        Order order = createOrder(1L);
        when(orderRepo.findAll()).thenReturn(List.of(order));

        List<OrderResponseDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_returnsDTO() {
        Order order = createOrder(1L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_throwsOrderNotFoundException() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void createOrder_createsOrderReducesStock() {
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(addressService.findById(1L)).thenReturn(testAddress);
        when(productService.findById(1L)).thenReturn(testProduct);

        Order savedOrder = createOrder(1L);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        OrderRequestDTO request = new OrderRequestDTO(
                1L, 1L,
                List.of(new OrderRequestDTO.OrderLineRequest(1L, 2)),
                "PENDING", "STANDARD", BigDecimal.valueOf(50)
        );

        OrderResponseDTO result = orderService.createOrderForCustomer(1L, request);

        assertThat(result).isNotNull();
        verify(productService).reduceQuantity(1L, 2);
    }

    @Test
    void createOrder_throwsExceptionWhenLowStock() {
        testProduct.setQuantity(1);
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(addressService.findById(1L)).thenReturn(testAddress);
        when(productService.findById(1L)).thenReturn(testProduct);

        OrderRequestDTO request = new OrderRequestDTO(
                1L, 1L,
                List.of(new OrderRequestDTO.OrderLineRequest(1L, 5)),
                "PENDING", "STANDARD", BigDecimal.valueOf(50)
        );

        assertThatThrownBy(() -> orderService.createOrderForCustomer(1L, request))
                .isInstanceOf(OutOfStockException.class);

        verify(orderRepo, never()).save(any());
    }

    @Test
    void markAsShipped_setsShippedToTrue() {
        Order order = createOrder(1L);
        assertThat(order.isShipped()).isFalse();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        OrderResponseDTO result = orderService.markAsShipped(1L);

        assertThat(result.isShipped()).isTrue();
    }

    @Test
    void deleteOrder_throwsOrderNotFoundException() {
        when(orderRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepo, never()).deleteById(any());
    }

    @Test
    void getTotalSold_returns0IfNoResult() {
        when(orderLineRepo.getTotalProductSoldBetweenDates(any(), any(), any())).thenReturn(null);

        Integer result = orderService.getTotalProductSoldBetweenDates(
                LocalDateTime.now().minusDays(7), LocalDateTime.now(), "Teaspoon"
        );

        assertThat(result).isEqualTo(0);
    }

    private Order createOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(testCustomer);
        order.setShippingAddress(testAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PENDING");
        order.setOrderType("STANDARD");
        order.setShipped(false);
        order.setShippingCharge(BigDecimal.valueOf(50));
        order.setTotalPrice(BigDecimal.valueOf(150));
        order.setOrderLines(new ArrayList<>());
        return order;
    }
}

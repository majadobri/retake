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

// Unit-test: tester OrderService uten database
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

    private Customer testKunde;
    private Address testAdresse;
    private Product testProdukt;

    @BeforeEach
    void setup() {
        testKunde = new Customer(1L, "Ola", "Nordmann", "ola@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());

        testAdresse = new Address();
        testAdresse.setId(1L);
        testAdresse.setStreet("Storgata 1");
        testAdresse.setCity("Oslo");
        testAdresse.setPostalCode("0150");
        testAdresse.setCountry("Norge");
        testAdresse.setCustomer(testKunde);

        testProdukt = new Product(1L, "Kaffe", "God kaffe", BigDecimal.valueOf(50), 10, ProductStatus.IN_STOCK);
    }

    @Test
    void getAllOrders_returnererListeMedDTOer() {
        Order ordre = lagOrdre(1L);
        when(orderRepo.findAll()).thenReturn(List.of(ordre));

        List<OrderResponseDTO> resultat = orderService.getAllOrders();

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_returnererDTO() {
        Order ordre = lagOrdre(1L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(ordre));

        OrderResponseDTO resultat = orderService.getOrderById(1L);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_kastarExceptionNaarIkkeFinnes() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void createOrder_oppretterOrdreOgReduserLager() {
        when(customerService.findById(1L)).thenReturn(testKunde);
        when(addressService.findById(1L)).thenReturn(testAdresse);
        when(productService.findById(1L)).thenReturn(testProdukt);

        Order lagretOrdre = lagOrdre(1L);
        when(orderRepo.save(any(Order.class))).thenReturn(lagretOrdre);

        OrderRequestDTO request = new OrderRequestDTO(
                1L, 1L,
                List.of(new OrderRequestDTO.OrderLineRequest(1L, 2)),
                "PENDING", "STANDARD", BigDecimal.valueOf(50)
        );

        OrderResponseDTO resultat = orderService.createOrderForCustomer(1L, request);

        assertThat(resultat).isNotNull();
        verify(productService).reduceQuantity(1L, 2);
    }

    @Test
    void createOrder_kastarExceptionNaarProduktErUtsolgt() {
        testProdukt.setQuantity(1); // Bare 1 på lager, men vi bestiller 5
        when(customerService.findById(1L)).thenReturn(testKunde);
        when(addressService.findById(1L)).thenReturn(testAdresse);
        when(productService.findById(1L)).thenReturn(testProdukt);

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
    void markAsShipped_setterShippedTilTrue() {
        Order ordre = lagOrdre(1L);
        assertThat(ordre.isShipped()).isFalse();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(ordre));
        when(orderRepo.save(any())).thenReturn(ordre);

        OrderResponseDTO resultat = orderService.markAsShipped(1L);

        assertThat(resultat.isShipped()).isTrue();
    }

    @Test
    void deleteOrder_kastarExceptionNaarIkkeFinnes() {
        when(orderRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepo, never()).deleteById(any());
    }

    @Test
    void getTotalSold_returnerer0NaarIngenResultat() {
        when(orderLineRepo.getTotalProductSoldBetweenDates(any(), any(), any())).thenReturn(null);

        Integer resultat = orderService.getTotalProductSoldBetweenDates(
                LocalDateTime.now().minusDays(7), LocalDateTime.now(), "Kaffe"
        );

        assertThat(resultat).isEqualTo(0);
    }

    // Hjelpemetode for å lage en komplett ordre for testing
    private Order lagOrdre(Long id) {
        Order ordre = new Order();
        ordre.setId(id);
        ordre.setCustomer(testKunde);
        ordre.setShippingAddress(testAdresse);
        ordre.setOrderDate(LocalDateTime.now());
        ordre.setOrderStatus("PENDING");
        ordre.setOrderType("STANDARD");
        ordre.setShipped(false);
        ordre.setShippingCharge(BigDecimal.valueOf(50));
        ordre.setTotalPrice(BigDecimal.valueOf(150));
        ordre.setOrderLines(new ArrayList<>());
        return ordre;
    }
}

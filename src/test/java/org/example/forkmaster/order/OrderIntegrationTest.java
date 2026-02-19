package org.example.forkmaster.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forkmaster.TestcontainersConfiguration;
import org.example.forkmaster.address.Address;
import org.example.forkmaster.address.AddressRepo;
import org.example.forkmaster.customer.Customer;
import org.example.forkmaster.customer.CustomerRepo;
import org.example.forkmaster.product.Product;
import org.example.forkmaster.product.ProductRepo;
import org.example.forkmaster.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer testCustomer;
    private Address testAddress;
    private Product testProduct;

    @BeforeEach
    void setupTestdata() {
        orderRepo.deleteAll();
        addressRepo.deleteAll();
        customerRepo.deleteAll();
        productRepo.deleteAll();

        testCustomer = new Customer();
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("User");
        testCustomer.setEmail("test@test.no");
        testCustomer.setPhone(12345678L);
        testCustomer = customerRepo.save(testCustomer);

        testAddress = new Address();
        testAddress.setStreet("Teststreet 1");
        testAddress.setCity("Oslo");
        testAddress.setPostalCode("0150");
        testAddress.setCountry("Norge");
        testAddress.setCustomer(testCustomer);
        testAddress = addressRepo.save(testAddress);

        testProduct = new Product();
        testProduct.setProductName("Test Product");
        testProduct.setDescription("For testing");
        testProduct.setPrice(BigDecimal.valueOf(100));
        testProduct.setQuantity(20);
        testProduct.setStatus(ProductStatus.IN_STOCK);
        testProduct = productRepo.save(testProduct);
    }

    @Test
    void getAllOrders_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createOrder_createsOrder() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(
                testCustomer.getId(),
                testAddress.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProduct.getId(), 2)),
                "PENDING",
                "STANDARD",
                BigDecimal.valueOf(50)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.shipped").value(false));
    }

    @Test
    void createOrder_throwsException() throws Exception {
        testProduct.setQuantity(0);
        productRepo.save(testProduct);

        OrderRequestDTO request = new OrderRequestDTO(
                testCustomer.getId(),
                testAddress.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProduct.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void markAsShipped_changesStatusToSent() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(
                testCustomer.getId(),
                testAddress.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProduct.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.valueOf(30)
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/orders/" + orderId + "/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipped").value(true));
    }

    @Test
    void deleteOrder_deletesOrder() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(
                testCustomer.getId(),
                testAddress.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProduct.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.ZERO
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());
    }
}

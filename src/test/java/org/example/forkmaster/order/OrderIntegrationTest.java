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

// Integrasjonstest: tester ordre-endepunkter mot ekte database
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

    // Testdata som opprettes før hver test
    private Customer testKunde;
    private Address testAdresse;
    private Product testProdukt;

    @BeforeEach
    void setupTestdata() {
        // Slett i riktig rekkefølge
        orderRepo.deleteAll();
        addressRepo.deleteAll();
        customerRepo.deleteAll();
        productRepo.deleteAll();

        // Opprett testkunde
        testKunde = new Customer();
        testKunde.setFirstName("Test");
        testKunde.setLastName("Bruker");
        testKunde.setEmail("test@test.no");
        testKunde.setPhone(12345678L);
        testKunde = customerRepo.save(testKunde);

        // Opprett testadresse
        testAdresse = new Address();
        testAdresse.setStreet("Testgata 1");
        testAdresse.setCity("Oslo");
        testAdresse.setPostalCode("0150");
        testAdresse.setCountry("Norge");
        testAdresse.setCustomer(testKunde);
        testAdresse = addressRepo.save(testAdresse);

        // Opprett testprodukt
        testProdukt = new Product();
        testProdukt.setProductName("Testprodukt");
        testProdukt.setDescription("For testing");
        testProdukt.setPrice(BigDecimal.valueOf(100));
        testProdukt.setQuantity(20);
        testProdukt.setStatus(ProductStatus.IN_STOCK);
        testProdukt = productRepo.save(testProdukt);
    }

    @Test
    void getAllOrders_returnererTomListeNaarIngenOrdrer() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createOrder_oppretterOrdreOgReturnerer201() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(
                testKunde.getId(),
                testAdresse.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProdukt.getId(), 2)),
                "PENDING",
                "STANDARD",
                BigDecimal.valueOf(50)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(testKunde.getId()))
                .andExpect(jsonPath("$.shipped").value(false));
    }

    @Test
    void createOrder_returnerFeilNaarProduktErUtsolgt() throws Exception {
        // Sett antall til 0 (utsolgt)
        testProdukt.setQuantity(0);
        productRepo.save(testProdukt);

        OrderRequestDTO request = new OrderRequestDTO(
                testKunde.getId(),
                testAdresse.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProdukt.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError()); // OutOfStockException
    }

    @Test
    void markAsShipped_setterOrdreTilSendt() throws Exception {
        // Opprett en ordre først
        OrderRequestDTO request = new OrderRequestDTO(
                testKunde.getId(),
                testAdresse.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProdukt.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.valueOf(30)
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long ordreId = objectMapper.readTree(response).get("id").asLong();

        // Marker som sendt
        mockMvc.perform(put("/api/orders/" + ordreId + "/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipped").value(true));
    }

    @Test
    void deleteOrder_sletterOrdren() throws Exception {
        // Opprett en ordre
        OrderRequestDTO request = new OrderRequestDTO(
                testKunde.getId(),
                testAdresse.getId(),
                List.of(new OrderRequestDTO.OrderLineRequest(testProdukt.getId(), 1)),
                "PENDING", "STANDARD", BigDecimal.ZERO
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long ordreId = objectMapper.readTree(response).get("id").asLong();

        // Slett ordren
        mockMvc.perform(delete("/api/orders/" + ordreId))
                .andExpect(status().isNoContent());
    }
}

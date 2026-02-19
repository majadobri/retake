package org.example.forkmaster.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forkmaster.TestcontainersConfiguration;
import org.example.forkmaster.customer.Customer;
import org.example.forkmaster.customer.CustomerRepo;
import org.example.forkmaster.order.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AddressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer testCustomer;

    @BeforeEach
    void clean() {
        orderRepo.deleteAll();
        addressRepo.deleteAll();
        customerRepo.deleteAll();

        testCustomer = new Customer();
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("User");
        testCustomer.setEmail("test@test.no");
        testCustomer.setPhone(12345678L);
        testCustomer = customerRepo.save(testCustomer);
    }

    @Test
    void getAllAddresses_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createAddress_returnsCreated() throws Exception {
        AddressDTO dto = new AddressDTO(null, "Storgata 1", "Oslo", "0150", "Norge", testCustomer.getId());

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.street").value("Storgata 1"))
                .andExpect(jsonPath("$.city").value("Oslo"))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getAddressById_returnsAddress() throws Exception {
        Address address = new Address();
        address.setStreet("Storgata 1");
        address.setCity("Oslo");
        address.setPostalCode("0150");
        address.setCountry("Norge");
        address.setCustomer(testCustomer);
        Address saved = addressRepo.save(address);

        mockMvc.perform(get("/api/addresses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Storgata 1"))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()));
    }

    @Test
    void getAddressById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/addresses/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressesByCustomerId_returnsAddressesForCustomer() throws Exception {
        Address address = new Address();
        address.setStreet("Storgata 1");
        address.setCity("Oslo");
        address.setPostalCode("0150");
        address.setCountry("Norge");
        address.setCustomer(testCustomer);
        addressRepo.save(address);

        mockMvc.perform(get("/api/addresses/customer/" + testCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].street").value("Storgata 1"));
    }

    @Test
    void deleteAddress_returnsNoContent() throws Exception {
        Address address = new Address();
        address.setStreet("Storgata 1");
        address.setCity("Oslo");
        address.setPostalCode("0150");
        address.setCountry("Norge");
        address.setCustomer(testCustomer);
        Address saved = addressRepo.save(address);

        mockMvc.perform(delete("/api/addresses/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(addressRepo.findById(saved.getId())).isEmpty();
    }
}

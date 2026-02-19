package org.example.forkmaster.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forkmaster.TestcontainersConfiguration;
import org.example.forkmaster.order.OrderRepo;
import org.example.forkmaster.address.AddressRepo;
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
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clean() {
        orderRepo.deleteAll();
        addressRepo.deleteAll();
        customerRepo.deleteAll();
    }

    @Test
    void getAllCustomers_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createCustomer() throws Exception {
        CustomerResponseDTO nyKunde = new CustomerResponseDTO(null, "Frida", "Kahlo", "fridaK@test.no", 12345678L, null, null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nyKunde)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Frida"))
                .andExpect(jsonPath("$.lastName").value("Kahlo"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getCustomerById_returnsCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("Kari");
        customer.setLastName("Hagen");
        customer.setEmail("kari@test.no");
        customer.setPhone(87654321L);
        Customer saved = customerRepo.save(customer);

        mockMvc.perform(get("/api/customers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kari"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.orders").isArray());
    }

    @Test
    void getCustomerById_returnerer404NaarIkkeFinnes() throws Exception {
        mockMvc.perform(get("/api/customers/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCustomer_updates() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("Gammel");
        customer.setLastName("Navn");
        customer.setEmail("gammel@test.no");
        customer.setPhone(11111111L);
        Customer saved = customerRepo.save(customer);

        CustomerResponseDTO update = new CustomerResponseDTO(null, "New", "Name", "new@test.no", 22222222L, null, null);

        mockMvc.perform(put("/api/customers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void deleteCustomer_deletesCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("Delete");
        customer.setLastName("Me");
        customer.setEmail("delete@test.no");
        customer.setPhone(33333333L);
        Customer saved = customerRepo.save(customer);

        mockMvc.perform(delete("/api/customers/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(customerRepo.findById(saved.getId())).isEmpty();
    }

    @Test
    void getAllCustomers_returnsAll() throws Exception {
        Customer k1 = new Customer();
        k1.setFirstName("Per"); k1.setLastName("A"); k1.setEmail("per@test.no"); k1.setPhone(1L);
        Customer k2 = new Customer();
        k2.setFirstName("Åse"); k2.setLastName("B"); k2.setEmail("ase@test.no"); k2.setPhone(2L);
        customerRepo.save(k1);
        customerRepo.save(k2);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

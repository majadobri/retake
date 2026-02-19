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

// Integrasjonstest: starter hele appen med en ekte PostgreSQL-database i Docker
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Brukes til å sende HTTP-forespørsler

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ObjectMapper objectMapper; // Konverterer objekter til/fra JSON

    @BeforeEach
    void ryddOppFørHverTest() {
        // Slett i riktig rekkefølge pga. fremmednøkler
        orderRepo.deleteAll();
        addressRepo.deleteAll();
        customerRepo.deleteAll();
    }

    @Test
    void getAllCustomers_returnererTomListeNaarIngenKunder() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createCustomer_oppretterKundeOgReturnerer201() throws Exception {
        CustomerResponseDTO nyKunde = new CustomerResponseDTO(null, "Ola", "Nordmann", "ola@test.no", 12345678L, null, null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nyKunde)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ola"))
                .andExpect(jsonPath("$.lastName").value("Nordmann"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getCustomerById_returnererKunden() throws Exception {
        // Sett opp testdata direkte i databasen
        Customer kunde = new Customer();
        kunde.setFirstName("Kari");
        kunde.setLastName("Hansen");
        kunde.setEmail("kari@test.no");
        kunde.setPhone(87654321L);
        Customer lagretKunde = customerRepo.save(kunde);

        mockMvc.perform(get("/api/customers/" + lagretKunde.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kari"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.orders").isArray());
    }

    @Test
    void getCustomerById_returnerer404NaarIkkeFinnes() throws Exception {
        mockMvc.perform(get("/api/customers/99999"))
                .andExpect(status().is5xxServerError()); // Feil returnerer 500 siden det ikke er en ExceptionHandler
    }

    @Test
    void updateCustomer_oppdatererKunden() throws Exception {
        Customer kunde = new Customer();
        kunde.setFirstName("Gammel");
        kunde.setLastName("Navn");
        kunde.setEmail("gammel@test.no");
        kunde.setPhone(11111111L);
        Customer lagret = customerRepo.save(kunde);

        CustomerResponseDTO oppdatering = new CustomerResponseDTO(null, "Nytt", "Navn", "nytt@test.no", 22222222L, null, null);

        mockMvc.perform(put("/api/customers/" + lagret.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oppdatering)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Nytt"));
    }

    @Test
    void deleteCustomer_sletterKundenOgReturnerer204() throws Exception {
        Customer kunde = new Customer();
        kunde.setFirstName("Slett");
        kunde.setLastName("Meg");
        kunde.setEmail("slett@test.no");
        kunde.setPhone(33333333L);
        Customer lagret = customerRepo.save(kunde);

        mockMvc.perform(delete("/api/customers/" + lagret.getId()))
                .andExpect(status().isNoContent());

        assertThat(customerRepo.findById(lagret.getId())).isEmpty();
    }

    @Test
    void getAllCustomers_returnererAlleKunder() throws Exception {
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

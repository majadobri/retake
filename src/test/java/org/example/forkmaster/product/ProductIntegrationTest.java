package org.example.forkmaster.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forkmaster.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integrasjonstest: tester alle produkt-endepunkter mot ekte database
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void ryddOpp() {
        productRepo.deleteAll();
    }

    @Test
    void getAllProducts_returnererTomListeNaarIngenProdukter() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createProduct_oppretterProduktOgReturnerer201() throws Exception {
        ProductDTO nyttProdukt = new ProductDTO(null, "Kaffemaskin", "Lager god kaffe", BigDecimal.valueOf(999), 10, ProductStatus.IN_STOCK);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nyttProdukt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Kaffemaskin"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("IN_STOCK"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getProductById_returnererProduktet() throws Exception {
        Product produkt = new Product();
        produkt.setProductName("Toaster");
        produkt.setDescription("Lager toast");
        produkt.setPrice(BigDecimal.valueOf(299));
        produkt.setQuantity(5);
        produkt.setStatus(ProductStatus.IN_STOCK);
        Product lagret = productRepo.save(produkt);

        mockMvc.perform(get("/api/products/" + lagret.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Toaster"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void updateProduct_oppdatererProduktet() throws Exception {
        Product produkt = new Product();
        produkt.setProductName("Gammelt produkt");
        produkt.setDescription("Gammel beskrivelse");
        produkt.setPrice(BigDecimal.valueOf(100));
        produkt.setQuantity(1);
        produkt.setStatus(ProductStatus.IN_STOCK);
        Product lagret = productRepo.save(produkt);

        ProductDTO oppdatering = new ProductDTO(null, "Nytt produkt", "Ny beskrivelse", BigDecimal.valueOf(200), 5, ProductStatus.IN_STOCK);

        mockMvc.perform(put("/api/products/" + lagret.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oppdatering)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Nytt produkt"))
                .andExpect(jsonPath("$.price").value(200));
    }

    @Test
    void deleteProduct_sletterProduktOgReturnerer204() throws Exception {
        Product produkt = new Product();
        produkt.setProductName("Slett meg");
        produkt.setDescription("Skal slettes");
        produkt.setPrice(BigDecimal.ONE);
        produkt.setQuantity(1);
        produkt.setStatus(ProductStatus.IN_STOCK);
        Product lagret = productRepo.save(produkt);

        mockMvc.perform(delete("/api/products/" + lagret.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepo.findById(lagret.getId())).isEmpty();
    }

    @Test
    void getLowStockProducts_returnererProduktMedLavtAntall() throws Exception {
        Product lav = new Product();
        lav.setProductName("Lite igjen");
        lav.setDescription("Nesten tomt");
        lav.setPrice(BigDecimal.valueOf(50));
        lav.setQuantity(2);
        lav.setStatus(ProductStatus.LOW_STOCK);
        productRepo.save(lav);

        Product fullStack = new Product();
        fullStack.setProductName("Masse igjen");
        fullStack.setDescription("Fullt lager");
        fullStack.setPrice(BigDecimal.valueOf(50));
        fullStack.setQuantity(100);
        fullStack.setStatus(ProductStatus.IN_STOCK);
        productRepo.save(fullStack);

        // Hent produkter med antall <= 5
        mockMvc.perform(get("/api/products/low-stock?maxQuantity=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Lite igjen"));
    }

    @Test
    void getProductsByStatus_returnererProduktMedGittStatus() throws Exception {
        Product utsolgt = new Product();
        utsolgt.setProductName("Utsolgt vare");
        utsolgt.setDescription("Borte");
        utsolgt.setPrice(BigDecimal.valueOf(10));
        utsolgt.setQuantity(0);
        utsolgt.setStatus(ProductStatus.OUT_OF_STOCK);
        productRepo.save(utsolgt);

        mockMvc.perform(get("/api/products/status/OUT_OF_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("OUT_OF_STOCK"));
    }
}

package org.example.forkmaster.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forkmaster.TestcontainersConfiguration;
import org.example.forkmaster.order.OrderRepo;
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
    @Autowired
    private OrderRepo orderRepo;

    @BeforeEach
    void clean() {
        orderRepo.deleteAll();
        productRepo.deleteAll();
    }

    @Test
    void getAllProducts_returnsEmptyListIfNoStock() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createProduct_creates() throws Exception {
        ProductDTO newProduct = new ProductDTO(null, "Knife", "Japanese", BigDecimal.valueOf(999), 10, ProductStatus.IN_STOCK);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Knife"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("IN_STOCK"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getProductById_returnsProduct() throws Exception {
        Product product = new Product();
        product.setProductName("Big knife");
        product.setDescription("Cuts deep");
        product.setPrice(BigDecimal.valueOf(299));
        product.setQuantity(5);
        product.setStatus(ProductStatus.IN_STOCK);
        Product saved = productRepo.save(product);

        mockMvc.perform(get("/api/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Big knife"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void updateProduct_updates() throws Exception {
        Product product = new Product();
        product.setProductName("old product");
        product.setDescription("old description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setQuantity(1);
        product.setStatus(ProductStatus.IN_STOCK);
        Product saved = productRepo.save(product);

        ProductDTO update = new ProductDTO(null, "New product", "New description", BigDecimal.valueOf(200), 5, ProductStatus.IN_STOCK);

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("New product"))
                .andExpect(jsonPath("$.price").value(200));
    }

    @Test
    void deleteProduct_deletes() throws Exception {
        Product product = new Product();
        product.setProductName("Delete me");
        product.setDescription("To be deleted");
        product.setPrice(BigDecimal.ONE);
        product.setQuantity(1);
        product.setStatus(ProductStatus.IN_STOCK);
        Product saved = productRepo.save(product);

        mockMvc.perform(delete("/api/products/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepo.findById(saved.getId())).isEmpty();
    }

    @Test
    void getLowStockProducts_returnsLowStock() throws Exception {
        Product low = new Product();
        low.setProductName("Low stock");
        low.setDescription("Almost empty");
        low.setPrice(BigDecimal.valueOf(50));
        low.setQuantity(2);
        low.setStatus(ProductStatus.LOW_STOCK);
        productRepo.save(low);

        Product fullStack = new Product();
        fullStack.setProductName("Much left");
        fullStack.setDescription("Full stock");
        fullStack.setPrice(BigDecimal.valueOf(50));
        fullStack.setQuantity(100);
        fullStack.setStatus(ProductStatus.IN_STOCK);
        productRepo.save(fullStack);

        mockMvc.perform(get("/api/products/low-stock?maxQuantity=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Low stock"));
    }

    @Test
    void getProductsByStatus_returnsStatus() throws Exception {
        Product soldOut = new Product();
        soldOut.setProductName("Sold out");
        soldOut.setDescription("Gone for ever");
        soldOut.setPrice(BigDecimal.valueOf(10));
        soldOut.setQuantity(0);
        soldOut.setStatus(ProductStatus.OUT_OF_STOCK);
        productRepo.save(soldOut);

        mockMvc.perform(get("/api/products/status/OUT_OF_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("OUT_OF_STOCK"));
    }
}

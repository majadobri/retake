package org.example.forkmaster.product;

import org.example.forkmaster.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepository;

    @InjectMocks
    private ProductService productService;

    private Product createProduct(Long id, String name, int number, ProductStatus status) {
        return new Product(id, name, "Description", BigDecimal.valueOf(100), number, status);
    }

    @Test
    void getAllProducts_returnsDTOs() {
        Product p = createProduct(1L, "Knife", 10, ProductStatus.IN_STOCK);
        when(productRepository.findAll()).thenReturn(List.of(p));

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Knife");
        assertThat(result.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void getProductById_returnsDTO() {
        Product p = createProduct(1L, "Spoon", 5, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Spoon");
    }

    @Test
    void getProductById_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createProduct_savesAndReturnsDTO() {
        Product saved = createProduct(1L, "Fork", 20, ProductStatus.IN_STOCK);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO input = new ProductDTO(null, "Fork", "Good fork", BigDecimal.valueOf(299), 20, ProductStatus.IN_STOCK);
        ProductDTO result = productService.createProduct(input);

        assertThat(result.getProductName()).isEqualTo("Fork");
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_updatesFieldsAndReturnsDTO() {
        Product existing = createProduct(1L, "Old name", 10, ProductStatus.IN_STOCK);
        Product updated = createProduct(1L, "New name", 15, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductDTO input = new ProductDTO(null, "New name", "New description", BigDecimal.valueOf(150), 15, ProductStatus.IN_STOCK);
        ProductDTO result = productService.updateProduct(1L, input);

        assertThat(result.getProductName()).isEqualTo("New name");
        assertThat(result.getQuantity()).isEqualTo(15);
    }

    @Test
    void deleteProductById_deletes() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProductById(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProductById_throwsProductNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void reduceQuantity_reducesStock() {
        Product p = createProduct(1L, "Fork", 10, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);

        productService.reduceQuantity(1L, 3);

        assertThat(p.getQuantity()).isEqualTo(7);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.IN_STOCK);
    }

    @Test
    void reduceQuantity_setsStatusOutOfStock() {
        Product p = createProduct(1L, "Last product", 2, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);

        productService.reduceQuantity(1L, 5);

        assertThat(p.getQuantity()).isEqualTo(0);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void getLowStockProducts_returnsLowStockProducts() {
        Product p = createProduct(1L, "Almost empty", 2, ProductStatus.LOW_STOCK);
        when(productRepository.findLowStockProducts(5)).thenReturn(List.of(p));

        List<ProductDTO> result = productService.getLowStockProducts(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void getProductsByStatus_returnsProducts() {
        Product p = createProduct(1L, "Sold out", 0, ProductStatus.OUT_OF_STOCK);
        when(productRepository.findByStatus(ProductStatus.OUT_OF_STOCK)).thenReturn(List.of(p));

        List<ProductDTO> result = productService.getProductsByStatus(ProductStatus.OUT_OF_STOCK);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }
}

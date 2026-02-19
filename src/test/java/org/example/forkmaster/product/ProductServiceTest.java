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

// Unit-test: tester ProductService uten database
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepository;

    @InjectMocks
    private ProductService productService;

    private Product lagProdukt(Long id, String navn, int antall, ProductStatus status) {
        return new Product(id, navn, "Beskrivelse", BigDecimal.valueOf(100), antall, status);
    }

    @Test
    void getAllProducts_returnererListeMedDTOer() {
        Product p = lagProdukt(1L, "Kaffemaskin", 10, ProductStatus.IN_STOCK);
        when(productRepository.findAll()).thenReturn(List.of(p));

        List<ProductDTO> resultat = productService.getAllProducts();

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getProductName()).isEqualTo("Kaffemaskin");
        assertThat(resultat.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void getProductById_returnererDTO() {
        Product p = lagProdukt(1L, "Toaster", 5, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ProductDTO resultat = productService.getProductById(1L);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getProductName()).isEqualTo("Toaster");
    }

    @Test
    void getProductById_kastarExceptionNaarProduktIkkeFinnes() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createProduct_lagrerOgReturnererDTO() {
        Product lagret = lagProdukt(1L, "Blender", 20, ProductStatus.IN_STOCK);
        when(productRepository.save(any(Product.class))).thenReturn(lagret);

        ProductDTO input = new ProductDTO(null, "Blender", "God blender", BigDecimal.valueOf(299), 20, ProductStatus.IN_STOCK);
        ProductDTO resultat = productService.createProduct(input);

        assertThat(resultat.getProductName()).isEqualTo("Blender");
        assertThat(resultat.getId()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_oppdatererFelteneOgReturnererDTO() {
        Product eksisterende = lagProdukt(1L, "Gammelt navn", 10, ProductStatus.IN_STOCK);
        Product oppdatert = lagProdukt(1L, "Nytt navn", 15, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(eksisterende));
        when(productRepository.save(any(Product.class))).thenReturn(oppdatert);

        ProductDTO input = new ProductDTO(null, "Nytt navn", "Ny beskrivelse", BigDecimal.valueOf(150), 15, ProductStatus.IN_STOCK);
        ProductDTO resultat = productService.updateProduct(1L, input);

        assertThat(resultat.getProductName()).isEqualTo("Nytt navn");
        assertThat(resultat.getQuantity()).isEqualTo(15);
    }

    @Test
    void deleteProductById_sletter() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProductById(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProductById_kastarExceptionNaarProduktIkkeFinnes() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void reduceQuantity_redusererAntall() {
        Product p = lagProdukt(1L, "Vann", 10, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);

        productService.reduceQuantity(1L, 3);

        // Etter reduksjon: 10 - 3 = 7
        assertThat(p.getQuantity()).isEqualTo(7);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.IN_STOCK);
    }

    @Test
    void reduceQuantity_setterStatusTilOutOfStockNaarAntallBlirNull() {
        Product p = lagProdukt(1L, "Siste varen", 2, ProductStatus.IN_STOCK);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);

        productService.reduceQuantity(1L, 5); // Reduserer med mer enn det finnes -> 0

        assertThat(p.getQuantity()).isEqualTo(0);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void getLowStockProducts_returnererProduktMedLavtAntall() {
        Product p = lagProdukt(1L, "Nesten tomt", 2, ProductStatus.LOW_STOCK);
        when(productRepository.findLowStockProducts(5)).thenReturn(List.of(p));

        List<ProductDTO> resultat = productService.getLowStockProducts(5);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void getProductsByStatus_returnererProduktMedGittStatus() {
        Product p = lagProdukt(1L, "Utsolgt produkt", 0, ProductStatus.OUT_OF_STOCK);
        when(productRepository.findByStatus(ProductStatus.OUT_OF_STOCK)).thenReturn(List.of(p));

        List<ProductDTO> resultat = productService.getProductsByStatus(ProductStatus.OUT_OF_STOCK);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }
}

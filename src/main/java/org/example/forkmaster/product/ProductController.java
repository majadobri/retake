package org.example.forkmaster.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products - fetching all products");
        List<ProductDTO> products = productService.findAll()
                .stream()
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getStatus()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - fetching product", id);
        Product product = productService.findById(id);
        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getStatus()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("POST /api/products - creating product: {}", productDTO.getProductName());

        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setStatus(productDTO.getStatus());

        Product saved = productService.saveProduct(product);

        ProductDTO result = new ProductDTO(
                saved.getId(),
                saved.getProductName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getQuantity(),
                saved.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        log.info("PUT /api/products/{} - updating product", id);

        Product existing = productService.findById(id);
        existing.setProductName(productDTO.getProductName());
        existing.setDescription(productDTO.getDescription());
        existing.setPrice(productDTO.getPrice());
        existing.setStatus(productDTO.getStatus());

        Product updated = productService.saveProduct(existing);
        ProductDTO result = new ProductDTO(
                updated.getId(),
                updated.getProductName(),
                updated.getDescription(),
                updated.getPrice(),
                updated.getQuantity(),
                updated.getStatus()
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - deleting product", id);
        productService.deleteProductById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestParam int maxQuantity) {
        log.info("GET /api/products/low-stock - fetching products with quantity <= {}", maxQuantity);

        List<ProductDTO> products = productService.findLowStockProducts(maxQuantity)
                .stream()
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getStatus()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductDTO>> getProductsByStatus(
            @PathVariable ProductStatus status) {
        log.info("GET /api/products/status/{} - fetching products", status);

        List<ProductDTO> products = productService.findByStatus(status)
                .stream()
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getStatus()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }
}

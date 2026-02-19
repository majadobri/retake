package org.example.forkmaster.product;

import org.example.forkmaster.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.forkmaster.product.ProductStatus.OUT_OF_STOCK;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepo productRepository;

    public ProductService(ProductRepo productRepository) {
        this.productRepository = productRepository;
    }


    public Product findById(Long id) {
        log.info("Finding product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFoundException("Product not found with id: " + id);
                });
    }

    public Product saveProduct(Product product) {
        Product saved = productRepository.save(product);
        log.info("Saved product with id: {}", saved.getId());
        return saved;
    }

    public void reduceQuantity(Long productId, int amount) {
        log.info("Reducing quantity for product {} by {}", productId, amount);
        Product product = findById(productId);
        int newQuantity = Math.max(0, product.getQuantity() - amount);
        product.setQuantity(newQuantity);
        if (newQuantity <= 0) {
            product.setStatus(OUT_OF_STOCK);
            log.warn("Product {} is now OUT_OF_STOCK", productId);
        }
        productRepository.save(product);
    }

    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(p -> new ProductDTO(p.getId(), p.getProductName(), p.getDescription(), p.getPrice(), p.getQuantity(), p.getStatus()))
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product p = findById(id);
        return new ProductDTO(p.getId(), p.getProductName(), p.getDescription(), p.getPrice(), p.getQuantity(), p.getStatus());
    }

    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating product: {}", dto.getProductName());
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setStatus(dto.getStatus());
        Product saved = saveProduct(product);
        return new ProductDTO(saved.getId(), saved.getProductName(), saved.getDescription(), saved.getPrice(), saved.getQuantity(), saved.getStatus());
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Updating product with id: {}", id);
        Product existing = findById(id);
        existing.setProductName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        existing.setStatus(dto.getStatus());
        Product updated = saveProduct(existing);
        return new ProductDTO(updated.getId(), updated.getProductName(), updated.getDescription(), updated.getPrice(), updated.getQuantity(), updated.getStatus());
    }

    public void deleteProductById(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<ProductDTO> getLowStockProducts(int maxQuantity) {
        log.info("Fetching products with quantity <= {}", maxQuantity);
        return productRepository.findLowStockProducts(maxQuantity).stream()
                .map(p -> new ProductDTO(p.getId(), p.getProductName(), p.getDescription(), p.getPrice(), p.getQuantity(), p.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByStatus(ProductStatus status) {
        log.info("Fetching products with status: {}", status);
        return productRepository.findByStatus(status).stream()
                .map(p -> new ProductDTO(p.getId(), p.getProductName(), p.getDescription(), p.getPrice(), p.getQuantity(), p.getStatus()))
                .collect(Collectors.toList());
    }
}
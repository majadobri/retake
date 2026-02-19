package org.example.forkmaster.product;

import org.example.forkmaster.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.forkmaster.product.ProductStatus.OUT_OF_STOCK;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepo productRepository;

    public ProductService(ProductRepo productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        log.info("Found {} products", products.size());
        return products;
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
        log.info("Saving product: {}", product.getProductName());
        Product saved = productRepository.save(product);
        log.info("Saved product with id: {}", saved.getId());
        return saved;
    }

    public void deleteProductById(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
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
        log.debug("Product {} new quantity: {}", productId, newQuantity);
    }

    public List<Product> findLowStockProducts(int maxQuantity) {
        log.info("Finding products with quantity <= {}", maxQuantity);
        return productRepository.findLowStockProducts(maxQuantity);
    }

    public List<Product> findByStatus(ProductStatus status) {
        log.info("Finding products with status: {}", status);
        return productRepository.findByStatus(status);
    }
}

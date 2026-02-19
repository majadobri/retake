package org.example.forkmaster.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p 
        WHERE p.quantity <= :maxQuantity 
        ORDER BY p.quantity ASC
    """)
    List<Product> findLowStockProducts(@Param("maxQuantity") int maxQuantity);

    @Query("SELECT p FROM Product p WHERE p.status = :status")
    List<Product> findByStatus(@Param("status") ProductStatus status);

    Optional<Product> findByProductName(String productName);
}

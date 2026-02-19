package org.example.forkmaster.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
interface OrderLineRepo extends JpaRepository<OrderLine, Long> {

    @Query("""
        SELECT SUM(ol.quantity)
        FROM OrderLine ol
        JOIN ol.order o
        WHERE o.orderDate BETWEEN :startDate AND :endDate
        AND ol.product.productName = :productName
    """)
    Integer getTotalProductSoldBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("productName") String productName
    );
}

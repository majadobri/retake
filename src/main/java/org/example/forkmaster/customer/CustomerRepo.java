package org.example.forkmaster.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    @Query("""
        SELECT c FROM Customer c
        WHERE (SELECT COUNT(o) FROM Order o WHERE o.customer.id = c.id) >= :minOrders
        ORDER BY (SELECT COUNT(o) FROM Order o WHERE o.customer.id = c.id) DESC
    """)
    List<Customer> findCustomersWithMinimumOrders(@Param("minOrders") int minOrders);
}

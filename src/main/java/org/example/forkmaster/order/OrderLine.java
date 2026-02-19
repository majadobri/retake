package org.example.forkmaster.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.forkmaster.product.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_line_gen")
    @SequenceGenerator(name = "order_line_gen", sequenceName = "order_line_gen", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference(value = "order-lines")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;

    public OrderLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = product.getPrice();
    }

    public BigDecimal getLineTotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}

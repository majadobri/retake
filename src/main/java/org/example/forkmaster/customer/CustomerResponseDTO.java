package org.example.forkmaster.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.forkmaster.address.AddressDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long phone;
    private List<AddressDTO> addresses;
    private List<OrderSummary> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Long id;
        private LocalDateTime orderDate;
        private BigDecimal totalPrice;
        private boolean shipped;
    }
}

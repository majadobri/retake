package org.example.forkmaster.customer;

import org.example.forkmaster.address.AddressDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        log.info("GET /api/customers - fetching all customers");
        List<CustomerResponseDTO> customers = customerService.findAll()
                .stream()
                .map(customer -> new CustomerResponseDTO(
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        null,
                        null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{} - fetching customer with details", id);
        Customer customer = customerService.findByIdWithDetails(id);

        List<AddressDTO> addresses = customer.getAddresses().stream()
                .map(a -> new AddressDTO(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry(), a.getCustomer().getId()))
                .collect(Collectors.toList());

        List<CustomerResponseDTO.OrderSummary> orders = customer.getOrders().stream()
                .map(o -> new CustomerResponseDTO.OrderSummary(o.getId(), o.getOrderDate(), o.getTotalPrice(), o.isShipped()))
                .collect(Collectors.toList());

        CustomerResponseDTO dto = new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                addresses,
                orders
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CustomerResponseDTO customerDTO) {
        log.info("POST /api/customers - creating customer: {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

        Customer customer = new Customer();
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhone(customerDTO.getPhone());

        Customer saved = customerService.saveCustomer(customer);

        CustomerResponseDTO result = new CustomerResponseDTO(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getPhone(),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerResponseDTO customerDTO) {
        log.info("PUT /api/customers/{} - updating customer", id);

        Customer existing = customerService.findById(id);
        existing.setFirstName(customerDTO.getFirstName());
        existing.setLastName(customerDTO.getLastName());
        existing.setEmail(customerDTO.getEmail());
        existing.setPhone(customerDTO.getPhone());

        Customer updated = customerService.saveCustomer(existing);

        CustomerResponseDTO result = new CustomerResponseDTO(
                updated.getId(),
                updated.getFirstName(),
                updated.getLastName(),
                updated.getEmail(),
                updated.getPhone(),
                null,
                null
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{} - deleting customer", id);
        customerService.deleteCustomerById(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping("/min-orders")
    public ResponseEntity<List<CustomerResponseDTO>> getCustomersWithMinimumOrders(@RequestParam int minOrders) {
        log.info("GET /api/customers/min-orders - fetching customers with at least {} orders", minOrders);
        List<CustomerResponseDTO> customers = customerService.findCustomersWithMinimumOrders(minOrders)
                .stream()
                .map(customer -> new CustomerResponseDTO(
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        null,
                        null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }
}

package org.example.forkmaster.customer;

import org.example.forkmaster.address.AddressDTO;
import org.example.forkmaster.exception.CustomerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    // --- Interne metoder brukt av andre services ---

    public Customer findById(Long id) {
        log.info("Finding customer by id: {}", id);
        return customerRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {}", id);
                    return new CustomerNotFoundException("Customer not found with id: " + id);
                });
    }

    @Transactional
    public Customer findByIdWithDetails(Long id) {
        log.info("Finding customer with addresses and orders by id: {}", id);
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {}", id);
                    return new CustomerNotFoundException("Customer not found with id: " + id);
                });
        customer.getAddresses().size();
        customer.getOrders().size();
        return customer;
    }

    public Customer saveCustomer(Customer customer) {
        Customer saved = customerRepo.save(customer);
        log.info("Saved customer with id: {}", saved.getId());
        return saved;
    }

    public List<Customer> findCustomersWithMinimumOrders(int minOrders) {
        return customerRepo.findCustomersWithMinimumOrders(minOrders);
    }

    // --- DTO-metoder brukt av controlleren ---

    public List<CustomerResponseDTO> getAllCustomers() {
        log.info("Fetching all customers");
        return findAll().stream()
                .map(c -> new CustomerResponseDTO(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), c.getPhone(), null, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Fetching customer with details for id: {}", id);
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        // Initialiser lazy-lister mens transaksjonen er aktiv
        List<AddressDTO> addresses = customer.getAddresses().stream()
                .map(a -> new AddressDTO(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry(), a.getCustomer().getId()))
                .collect(Collectors.toList());
        List<CustomerResponseDTO.OrderSummary> orders = customer.getOrders().stream()
                .map(o -> new CustomerResponseDTO.OrderSummary(o.getId(), o.getOrderDate(), o.getTotalPrice(), o.isShipped()))
                .collect(Collectors.toList());
        return new CustomerResponseDTO(customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), addresses, orders);
    }

    public CustomerResponseDTO createCustomer(CustomerResponseDTO dto) {
        log.info("Creating customer: {} {}", dto.getFirstName(), dto.getLastName());
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        Customer saved = saveCustomer(customer);
        return new CustomerResponseDTO(saved.getId(), saved.getFirstName(), saved.getLastName(),
                saved.getEmail(), saved.getPhone(), null, null);
    }

    public CustomerResponseDTO updateCustomer(Long id, CustomerResponseDTO dto) {
        log.info("Updating customer with id: {}", id);
        Customer existing = findById(id);
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        Customer updated = saveCustomer(existing);
        return new CustomerResponseDTO(updated.getId(), updated.getFirstName(), updated.getLastName(),
                updated.getEmail(), updated.getPhone(), null, null);
    }

    public void deleteCustomerById(Long id) {
        log.info("Deleting customer with id: {}", id);
        if (!customerRepo.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepo.deleteById(id);
    }

    public List<CustomerResponseDTO> getCustomersWithMinimumOrders(int minOrders) {
        log.info("Fetching customers with at least {} orders", minOrders);
        return findCustomersWithMinimumOrders(minOrders).stream()
                .map(c -> new CustomerResponseDTO(c.getId(), c.getFirstName(), c.getLastName(),
                        c.getEmail(), c.getPhone(), null, null))
                .collect(Collectors.toList());
    }
}
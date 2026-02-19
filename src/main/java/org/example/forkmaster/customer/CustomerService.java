package org.example.forkmaster.customer;

import org.example.forkmaster.exception.CustomerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public List<Customer> findAll() {
        List<Customer> customers = customerRepo.findAll();
        log.info("Found {} customers", customers.size());
        return customers;
    }

    public Customer findById(Long id) {
        log.info("Finding customer by id: {}", id);
        return customerRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {}", id);
                    return new CustomerNotFoundException("Customer not found with id: " + id);
                });
    }

    public Customer saveCustomer(Customer customer) {
        log.info("Saving customer: {} {}", customer.getFirstName(), customer.getLastName());
        Customer saved = customerRepo.save(customer);
        log.info("Saved customer with id: {}", saved.getId());
        return saved;
    }

    public void deleteCustomerById(Long id) {
        log.info("Deleting customer with id: {}", id);
        if (!customerRepo.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepo.deleteById(id);
    }

    public List<Customer> findCustomersWithMinimumOrders(int minOrders) {
        log.info("Finding customers with at least {} orders", minOrders);
        return customerRepo.findCustomersWithMinimumOrders(minOrders);
    }
}

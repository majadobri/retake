package org.example.forkmaster.customer;

import org.example.forkmaster.exception.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepo customerRepo;

    @InjectMocks
    private CustomerService customerService;

    private Customer createCustomer(Long id, String fornavn, String etternavn) {
        return new Customer(id, fornavn, etternavn, fornavn.toLowerCase() + "@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void getAllCustomers_returnsDTOs() {
        Customer customer = createCustomer(1L, "Frida", "Hansen");
        when(customerRepo.findAll()).thenReturn(List.of(customer));

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Frida");
        assertThat(result.get(0).getLastName()).isEqualTo("Hansen");
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllCustomers_returnsEmptyList() {
        when(customerRepo.findAll()).thenReturn(List.of());

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertThat(result).isEmpty();
    }

    @Test
    void getCustomerById_returnsDTO() {
        Customer customer = createCustomer(1L, "Kari", "Hansen");
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Kari");
        assertThat(result.getAddresses()).isEmpty();
        assertThat(result.getOrders()).isEmpty();
    }

    @Test
    void getCustomerById_throwsCustomerNotFoundException() {
        when(customerRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCustomer() {
        Customer saved = createCustomer(1L, "Per", "Olsen");
        when(customerRepo.save(any(Customer.class))).thenReturn(saved);

        CustomerResponseDTO input = new CustomerResponseDTO(null, "Per", "Olsen", "per@test.no", 99999999L, null, null);
        CustomerResponseDTO result = customerService.createCustomer(input);

        assertThat(result.getFirstName()).isEqualTo("Per");
        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepo).save(any(Customer.class));
    }

    @Test
    void updateCustomer_updatesCustomer() {
        Customer existing = createCustomer(1L, "Old", "Name");
        Customer updated = createCustomer(1L, "New", "Name");
        when(customerRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepo.save(any(Customer.class))).thenReturn(updated);

        CustomerResponseDTO input = new CustomerResponseDTO(null, "New", "Name", "new@test.no", 11111111L, null, null);
        CustomerResponseDTO result = customerService.updateCustomer(1L, input);

        assertThat(result.getFirstName()).isEqualTo("New");
    }

    @Test
    void updateCustomer_throwsCustomerNotFoundException() {
        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        CustomerResponseDTO input = new CustomerResponseDTO(null, "Test", "Test", "t@t.no", 1L, null, null);

        assertThatThrownBy(() -> customerService.updateCustomer(99L, input))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void deleteCustomerById_deletesCustomer() {
        when(customerRepo.existsById(1L)).thenReturn(true);

        customerService.deleteCustomerById(1L);

        verify(customerRepo).deleteById(1L);
    }

    @Test
    void deleteCustomerById_customerNotFoundException() {
        when(customerRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepo, never()).deleteById(any());
    }

    @Test
    void getCustomersWithMinimumOrders_returnsList() {
        Customer customer = createCustomer(1L, "Big", "Buyer");
        when(customerRepo.findCustomersWithMinimumOrders(3)).thenReturn(List.of(customer));

        List<CustomerResponseDTO> result = customerService.getCustomersWithMinimumOrders(3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Big");
    }
}

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

// Unit-test: tester CustomerService uten database (bruker Mockito for å fake repo-kall)
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepo customerRepo;

    @InjectMocks
    private CustomerService customerService;

    // Hjelpemetode for å lage en testkundeobjekt
    private Customer lagKunde(Long id, String fornavn, String etternavn) {
        return new Customer(id, fornavn, etternavn, fornavn.toLowerCase() + "@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void getAllCustomers_returnererListeMedDTOer() {
        // Arrange - sett opp hva repo skal returnere
        Customer kunde = lagKunde(1L, "Ola", "Nordmann");
        when(customerRepo.findAll()).thenReturn(List.of(kunde));

        // Act - kall metoden vi tester
        List<CustomerResponseDTO> resultat = customerService.getAllCustomers();

        // Assert - sjekk at resultatet er riktig
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getFirstName()).isEqualTo("Ola");
        assertThat(resultat.get(0).getLastName()).isEqualTo("Nordmann");
        assertThat(resultat.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllCustomers_returnererTomListeNaarIngenKunder() {
        when(customerRepo.findAll()).thenReturn(List.of());

        List<CustomerResponseDTO> resultat = customerService.getAllCustomers();

        assertThat(resultat).isEmpty();
    }

    @Test
    void getCustomerById_returnererDTO() {
        Customer kunde = lagKunde(1L, "Kari", "Hansen");
        when(customerRepo.findById(1L)).thenReturn(Optional.of(kunde));

        CustomerResponseDTO resultat = customerService.getCustomerById(1L);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getFirstName()).isEqualTo("Kari");
        assertThat(resultat.getAddresses()).isEmpty();
        assertThat(resultat.getOrders()).isEmpty();
    }

    @Test
    void getCustomerById_kastarExceptionNaarKundeIkkeFinnes() {
        when(customerRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCustomer_lagrerOgReturnererDTO() {
        Customer lagretKunde = lagKunde(1L, "Per", "Olsen");
        when(customerRepo.save(any(Customer.class))).thenReturn(lagretKunde);

        CustomerResponseDTO input = new CustomerResponseDTO(null, "Per", "Olsen", "per@test.no", 99999999L, null, null);
        CustomerResponseDTO resultat = customerService.createCustomer(input);

        assertThat(resultat.getFirstName()).isEqualTo("Per");
        assertThat(resultat.getId()).isEqualTo(1L);
        verify(customerRepo).save(any(Customer.class));
    }

    @Test
    void updateCustomer_oppdatererFelteneOgReturnererDTO() {
        Customer eksisterende = lagKunde(1L, "Gammel", "Navn");
        Customer oppdatert = lagKunde(1L, "Ny", "Navn");
        when(customerRepo.findById(1L)).thenReturn(Optional.of(eksisterende));
        when(customerRepo.save(any(Customer.class))).thenReturn(oppdatert);

        CustomerResponseDTO input = new CustomerResponseDTO(null, "Ny", "Navn", "ny@test.no", 11111111L, null, null);
        CustomerResponseDTO resultat = customerService.updateCustomer(1L, input);

        assertThat(resultat.getFirstName()).isEqualTo("Ny");
    }

    @Test
    void updateCustomer_kastarExceptionNaarKundeIkkeFinnes() {
        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        CustomerResponseDTO input = new CustomerResponseDTO(null, "Test", "Test", "t@t.no", 1L, null, null);

        assertThatThrownBy(() -> customerService.updateCustomer(99L, input))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void deleteCustomerById_sletter() {
        when(customerRepo.existsById(1L)).thenReturn(true);

        customerService.deleteCustomerById(1L);

        verify(customerRepo).deleteById(1L);
    }

    @Test
    void deleteCustomerById_kastarExceptionNaarKundeIkkeFinnes() {
        when(customerRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepo, never()).deleteById(any());
    }

    @Test
    void getCustomersWithMinimumOrders_returnererListe() {
        Customer kunde = lagKunde(1L, "Stor", "Kjøper");
        when(customerRepo.findCustomersWithMinimumOrders(3)).thenReturn(List.of(kunde));

        List<CustomerResponseDTO> resultat = customerService.getCustomersWithMinimumOrders(3);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getFirstName()).isEqualTo("Stor");
    }
}

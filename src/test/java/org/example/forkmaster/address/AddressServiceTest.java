package org.example.forkmaster.address;

import org.example.forkmaster.customer.Customer;
import org.example.forkmaster.customer.CustomerService;
import org.example.forkmaster.exception.AddressNotFoundException;
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

// Unit-test: tester AddressService uten database
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AddressService addressService;

    private Customer lagKunde(Long id) {
        return new Customer(id, "Ola", "Nordmann", "ola@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());
    }

    private Address lagAdresse(Long id, Customer kunde) {
        Address a = new Address();
        a.setId(id);
        a.setStreet("Storgata 1");
        a.setCity("Oslo");
        a.setPostalCode("0150");
        a.setCountry("Norge");
        a.setCustomer(kunde);
        return a;
    }

    @Test
    void getAllAddresses_returnererListeMedDTOer() {
        Customer kunde = lagKunde(1L);
        Address adresse = lagAdresse(1L, kunde);
        when(addressRepo.findAll()).thenReturn(List.of(adresse));

        List<AddressDTO> resultat = addressService.getAllAddresses();

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getStreet()).isEqualTo("Storgata 1");
        assertThat(resultat.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void getAddressById_returnererDTO() {
        Customer kunde = lagKunde(1L);
        Address adresse = lagAdresse(1L, kunde);
        when(addressRepo.findById(1L)).thenReturn(Optional.of(adresse));

        AddressDTO resultat = addressService.getAddressById(1L);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getCity()).isEqualTo("Oslo");
    }

    @Test
    void getAddressById_kastarExceptionNaarIkkeFinnes() {
        when(addressRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(99L))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAddressesByCustomerId_returnererListeMedDTOer() {
        Customer kunde = lagKunde(1L);
        Address adresse = lagAdresse(1L, kunde);
        when(addressRepo.findByCustomerId(1L)).thenReturn(List.of(adresse));

        List<AddressDTO> resultat = addressService.getAddressesByCustomerId(1L);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void createAddress_lagrerOgReturnererDTO() {
        Customer kunde = lagKunde(1L);
        Address lagretAdresse = lagAdresse(1L, kunde);
        when(customerService.findById(1L)).thenReturn(kunde);
        when(addressRepo.save(any(Address.class))).thenReturn(lagretAdresse);

        AddressDTO input = new AddressDTO(null, "Storgata 1", "Oslo", "0150", "Norge", 1L);
        AddressDTO resultat = addressService.createAddress(input);

        assertThat(resultat.getStreet()).isEqualTo("Storgata 1");
        assertThat(resultat.getCustomerId()).isEqualTo(1L);
        verify(addressRepo).save(any(Address.class));
    }

    @Test
    void deleteAddressById_sletter() {
        when(addressRepo.existsById(1L)).thenReturn(true);

        addressService.deleteAddressById(1L);

        verify(addressRepo).deleteById(1L);
    }

    @Test
    void deleteAddressById_kastarExceptionNaarIkkeFinnes() {
        when(addressRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> addressService.deleteAddressById(99L))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepo, never()).deleteById(any());
    }
}

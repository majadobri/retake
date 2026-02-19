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

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AddressService addressService;

    private Customer createCustomer(Long id) {
        return new Customer(id, "Frida", "Kahlo", "fridaK@test.no", 12345678L, new ArrayList<>(), new ArrayList<>());
    }

    private Address createAddress(Long id, Customer kunde) {
        Address a = new Address();
        a.setId(id);
        a.setStreet("Elias Blix´gate 3");
        a.setCity("Oslo");
        a.setPostalCode("0171");
        a.setCountry("Norge");
        a.setCustomer(kunde);
        return a;
    }

    @Test
    void getAllAddresses_returnsDTOs() {
        Customer customer = createCustomer(1L);
        Address address = createAddress(1L, customer);
        when(addressRepo.findAll()).thenReturn(List.of(address));

        List<AddressDTO> result = addressService.getAllAddresses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStreet()).isEqualTo("Storgata 1");
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void getAddressById_returnsDTO() {
        Customer customer = createCustomer(1L);
        Address address = createAddress(1L, customer);
        when(addressRepo.findById(1L)).thenReturn(Optional.of(address));

        AddressDTO result = addressService.getAddressById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCity()).isEqualTo("Oslo");
    }

    @Test
    void getAddressById_throwsException_whenAddressNotFound() {
        when(addressRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(99L))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAddressesByCustomerId_returnsListOfDTOs() {
        Customer customer = createCustomer(1L);
        Address address = createAddress(1L, customer);
        when(addressRepo.findByCustomerId(1L)).thenReturn(List.of(address));

        List<AddressDTO> result = addressService.getAddressesByCustomerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void createAddress_lagrerOgReturnererDTO() {
        Customer customer = createCustomer(1L);
        Address savedAddr = createAddress(1L, customer);
        when(customerService.findById(1L)).thenReturn(customer);
        when(addressRepo.save(any(Address.class))).thenReturn(savedAddr);

        AddressDTO input = new AddressDTO(null, "Storgata 1", "Oslo", "0150", "Norge", 1L);
        AddressDTO result = addressService.createAddress(input);

        assertThat(result.getStreet()).isEqualTo("Storgata 1");
        assertThat(result.getCustomerId()).isEqualTo(1L);
        verify(addressRepo).save(any(Address.class));
    }

    @Test
    void deleteAddressById_deletes() {
        when(addressRepo.existsById(1L)).thenReturn(true);

        addressService.deleteAddressById(1L);

        verify(addressRepo).deleteById(1L);
    }

    @Test
    void deleteAddressById_throwsException_whenAddressNotFound() {
        when(addressRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> addressService.deleteAddressById(99L))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepo, never()).deleteById(any());
    }
}

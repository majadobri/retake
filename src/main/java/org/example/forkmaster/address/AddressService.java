package org.example.forkmaster.address;

import org.example.forkmaster.customer.CustomerService;
import org.example.forkmaster.exception.AddressNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    private final AddressRepo addressRepo;
    private final CustomerService customerService;

    public AddressService(AddressRepo addressRepo, CustomerService customerService) {
        this.addressRepo = addressRepo;
        this.customerService = customerService;
    }

    public Address findById(Long id) {
        log.info("Finding address by id: {}", id);
        return addressRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Address not found with id: {}", id);
                    return new AddressNotFoundException("Address not found with id: " + id);
                });
    }

    public List<AddressDTO> getAllAddresses() {
        log.info("Fetching all addresses");
        return addressRepo.findAll().stream()
                .map(a -> new AddressDTO(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry(), a.getCustomer().getId()))
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long id) {
        Address a = findById(id);
        return new AddressDTO(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry(), a.getCustomer().getId());
    }

    public List<AddressDTO> getAddressesByCustomerId(Long customerId) {
        log.info("Fetching addresses for customer: {}", customerId);
        return addressRepo.findByCustomerId(customerId).stream()
                .map(a -> new AddressDTO(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry(), a.getCustomer().getId()))
                .collect(Collectors.toList());
    }

    public AddressDTO createAddress(AddressDTO addressDTO) {
        log.info("Creating address: {}, {}", addressDTO.getStreet(), addressDTO.getCity());
        Address address = new Address();
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setCustomer(customerService.findById(addressDTO.getCustomerId()));
        Address saved = addressRepo.save(address);
        return new AddressDTO(saved.getId(), saved.getStreet(), saved.getCity(), saved.getPostalCode(), saved.getCountry(), saved.getCustomer().getId());
    }

    public void deleteAddressById(Long id) {
        log.info("Deleting address with id: {}", id);
        if (!addressRepo.existsById(id)) {
            throw new AddressNotFoundException("Address not found with id: " + id);
        }
        addressRepo.deleteById(id);
    }

}
package org.example.forkmaster.address;

import org.example.forkmaster.exception.AddressNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    private final AddressRepo addressRepo;

    public AddressService(AddressRepo addressRepo) {
        this.addressRepo = addressRepo;
    }

    public List<Address> findAll() {
        List<Address> addresses = addressRepo.findAll();
        log.info("Found {} addresses", addresses.size());
        return addresses;
    }

    public Address findById(Long id) {
        log.info("Finding address by id: {}", id);
        return addressRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Address not found with id: {}", id);
                    return new AddressNotFoundException("Address not found with id: " + id);
                });
    }

    public Address saveAddress(Address address) {
        log.info("Saving address: {}, {}", address.getStreet(), address.getCity());
        Address saved = addressRepo.save(address);
        log.info("Saved address with id: {}", saved.getId());
        return saved;
    }

    public void deleteAddressById(Long id) {
        log.info("Deleting address with id: {}", id);
        if (!addressRepo.existsById(id)) {
            throw new AddressNotFoundException("Address not found with id: " + id);
        }
        addressRepo.deleteById(id);
    }

    public List<Address> findByCustomerId(Long customerId) {
        log.info("Finding addresses for customer: {}", customerId);
        return addressRepo.findByCustomerId(customerId);
    }
}

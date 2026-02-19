package org.example.forkmaster.address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        log.info("GET /api/addresses - fetching all addresses");
        List<AddressDTO> addresses = addressService.findAll()
                .stream()
                .map(address -> new AddressDTO(
                        address.getId(),
                        address.getStreet(),
                        address.getCity(),
                        address.getPostalCode(),
                        address.getCountry(),
                        address.getCustomer().getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        log.info("GET /api/addresses/{} - fetching address", id);
        Address address = addressService.findById(id);
        AddressDTO dto = new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry(),
                address.getCustomer().getId()
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/addresses/customer/{} - fetching addresses for customer", customerId);
        List<AddressDTO> addresses = addressService.findByCustomerId(customerId)
                .stream()
                .map(address -> new AddressDTO(
                        address.getId(),
                        address.getStreet(),
                        address.getCity(),
                        address.getPostalCode(),
                        address.getCountry(),
                        address.getCustomer().getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO) {
        log.info("POST /api/addresses - creating address: {}, {}", addressDTO.getStreet(), addressDTO.getCity());
        Address saved = addressService.createAddress(addressDTO);
        AddressDTO result = new AddressDTO(
                saved.getId(),
                saved.getStreet(),
                saved.getCity(),
                saved.getPostalCode(),
                saved.getCountry(),
                saved.getCustomer().getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long id) {
        log.info("DELETE /api/addresses/{} - deleting address", id);
        addressService.deleteAddressById(id);
        return ResponseEntity.ok("Address deleted successfully");
    }
}

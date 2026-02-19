package org.example.forkmaster.address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Slf4j
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        log.info("GET /api/addresses");
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        log.info("GET /api/addresses/{}", id);
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/addresses/customer/{}", customerId);
        return ResponseEntity.ok(addressService.getAddressesByCustomerId(customerId));
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
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        log.info("DELETE /api/addresses/{}", id);
        addressService.deleteAddressById(id);
        return ResponseEntity.noContent().build();
    }
}
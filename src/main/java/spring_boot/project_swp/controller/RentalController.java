package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.service.RentalService;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalController {

    final RentalService rentalService;

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(@RequestBody @Valid RentalRequest request) {
        return new ResponseEntity<>(rentalService.createRental(request), HttpStatus.CREATED);
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable Integer rentalId) {
        return new ResponseEntity<>(rentalService.getRentalById(rentalId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<RentalResponse>> getAllRentals() {
        return new ResponseEntity<>(rentalService.getAllRentals(), HttpStatus.OK);
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<RentalResponse>> getRentalsByRenterId(@PathVariable Integer renterId) {
        return new ResponseEntity<>(rentalService.getRentalsByRenterId(renterId), HttpStatus.OK);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<RentalResponse>> getRentalsByVehicleId(@PathVariable Integer vehicleId) {
        return new ResponseEntity<>(rentalService.getRentalsByVehicleId(vehicleId), HttpStatus.OK);
    }

    @PutMapping("/{rentalId}")
    public ResponseEntity<RentalResponse> updateRental(@PathVariable Integer rentalId, @RequestBody @Valid RentalRequest request) {
        return new ResponseEntity<>(rentalService.updateRental(rentalId, request), HttpStatus.OK);
    }

    @DeleteMapping("/{rentalId}")
    public ResponseEntity<Void> deleteRental(@PathVariable Integer rentalId) {
        rentalService.deleteRental(rentalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
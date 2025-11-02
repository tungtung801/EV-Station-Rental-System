package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.service.RentalService;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Rental APIs", description = "APIs for managing vehicle rentals")
public class RentalController {

  final RentalService rentalService;

  @PostMapping
  @Operation(summary = "Create a new rental", description = "Creates a new vehicle rental record.")
  public ResponseEntity<RentalResponse> createRental(@RequestBody @Valid RentalRequest request) {
    return new ResponseEntity<>(rentalService.createRental(request), HttpStatus.CREATED);
  }

  @GetMapping("/{rentalId}")
  @Operation(
      summary = "Get rental by ID",
      description = "Retrieves a rental record by its unique ID.")
  public ResponseEntity<RentalResponse> getRentalById(@PathVariable Long rentalId) {
    return new ResponseEntity<>(rentalService.getRentalById(rentalId), HttpStatus.OK);
  }

  @GetMapping
  @Operation(summary = "Get all rentals", description = "Retrieves a list of all rental records.")
  public ResponseEntity<List<RentalResponse>> getAllRentals() {
    return new ResponseEntity<>(rentalService.getAllRentals(), HttpStatus.OK);
  }

  @GetMapping("/renter/{renterId}")
  @Operation(
      summary = "Get rentals by renter ID",
      description = "Retrieves a list of rental records for a specific renter.")
  public ResponseEntity<List<RentalResponse>> getRentalsByRenterId(@PathVariable Long renterId) {
    return new ResponseEntity<>(rentalService.getRentalsByRenterId(renterId), HttpStatus.OK);
  }

  @GetMapping("/vehicle/{vehicleId}")
  @Operation(
      summary = "Get rentals by vehicle ID",
      description = "Retrieves a list of rental records for a specific vehicle.")
  public ResponseEntity<List<RentalResponse>> getRentalsByVehicleId(@PathVariable Long vehicleId) {
    return new ResponseEntity<>(rentalService.getRentalsByVehicleId(vehicleId), HttpStatus.OK);
  }

  @PutMapping("/{rentalId}")
  @Operation(
      summary = "Update an existing rental",
      description = "Updates an existing rental record.")
  public ResponseEntity<RentalResponse> updateRental(
      @PathVariable Long rentalId, @RequestBody @Valid RentalRequest request) {
    return new ResponseEntity<>(rentalService.updateRental(rentalId, request), HttpStatus.OK);
  }

  @DeleteMapping("/{rentalId}")
  @Operation(summary = "Delete a rental", description = "Deletes a rental record by its ID.")
  public ResponseEntity<Void> deleteRental(@PathVariable Long rentalId) {
    rentalService.deleteRental(rentalId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

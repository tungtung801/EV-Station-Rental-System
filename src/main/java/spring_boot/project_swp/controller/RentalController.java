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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.service.RentalService;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Rental APIs", description = "APIs for managing vehicle rentals")
public class RentalController {

  RentalService rentalService;

  @PostMapping("/create-from-booking/{bookingId}")
  @Operation(
      summary = "Create a rental from a booking",
      description = "Creates a new rental based on an existing booking.")
  public ResponseEntity<RentalResponse> createRentalFromBooking(@PathVariable Long bookingId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(rentalService.createRentalFromBooking(bookingId));
  }

  @PutMapping("/{rentalId}/confirm-pickup")
  @Operation(
      summary = "Confirm vehicle pickup",
      description = "Confirms the pickup of a vehicle for a rental.")
  public ResponseEntity<RentalResponse> confirmPickup(
      @PathVariable Long rentalId, @RequestBody @Valid RentalConfirmPickupRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    return ResponseEntity.ok(
        rentalService.confirmPickup(rentalId, staffEmail, request.getContractUrl()));
  }

  @PutMapping("/{rentalId}/confirm-return")
  @Operation(
      summary = "Confirm vehicle return",
      description = "Confirms the return of a vehicle for a rental.")
  public ResponseEntity<RentalResponse> confirmReturn(@PathVariable Long rentalId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    return ResponseEntity.ok(rentalService.confirmReturn(rentalId, staffEmail));
  }

  @GetMapping("/{rentalId}")
  @Operation(summary = "Get rental by ID", description = "Retrieves a rental by its unique ID.")
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    return new ResponseEntity<>(
        rentalService.updateRental(rentalId, userEmail, request), HttpStatus.OK);
  }

  @DeleteMapping("/{rentalId}")
  @Operation(summary = "Delete a rental", description = "Deletes a rental record by its ID.")
  public ResponseEntity<Void> deleteRental(@PathVariable Long rentalId) {
    rentalService.deleteRental(rentalId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

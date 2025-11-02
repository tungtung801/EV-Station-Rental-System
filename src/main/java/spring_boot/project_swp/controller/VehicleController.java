package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.VehicleService;

@RestController
@RequestMapping("/api/vehicles")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Vehicle APIs", description = "APIs for managing vehicles")
public class VehicleController {
  final VehicleService vehicleService;
  final BookingService bookingService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new vehicle", description = "Adds a new vehicle to the system.")
  public ResponseEntity<VehicleResponse> createVehicle(
      @Valid @ModelAttribute VehicleRequest request) {
    VehicleResponse newVehicle = vehicleService.addVehicle(request);
    return new ResponseEntity<>(newVehicle, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Update vehicle details",
      description = "Updates an existing vehicle's information.")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable Long id, @Valid @ModelAttribute VehicleRequest request) {
    VehicleResponse updated = vehicleService.updateVehicle(id, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a vehicle", description = "Deletes a vehicle by its unique ID.")
  public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
    vehicleService.deleteVehicle(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(
      summary = "Get all vehicles",
      description = "Retrieves a list of all registered vehicles.")
  public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
    return ResponseEntity.ok(vehicleService.findAll());
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get vehicle by ID",
      description = "Retrieves a vehicle's details by its unique ID.")
  public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleService.findById(id));
  }

  @GetMapping("/{vehicleId}/active-bookings") // URL mới: /api/vehicles/{vehicleId}/active-bookings
  @Operation(
      summary = "Get active bookings for a vehicle",
      description = "Retrieves a list of active bookings for a specific vehicle.")
  public ResponseEntity<List<BookingResponse>> get3OnGoingBookings(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(bookingService.get3OnGoingBookingsOfVehicle(vehicleId));
  }
}

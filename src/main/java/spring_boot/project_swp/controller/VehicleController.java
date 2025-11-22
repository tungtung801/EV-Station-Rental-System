package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor; // <--- SỬA
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.VehicleService;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor // <--- Dùng cái này
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Vehicle APIs", description = "APIs for managing vehicles")
public class VehicleController {

  final VehicleService vehicleService;
  final BookingService bookingService; // Lưu ý: Em cần tạo BookingService sau

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new vehicle")
  public ResponseEntity<VehicleResponse> createVehicle(
      @Valid @ModelAttribute VehicleRequest request) {
    VehicleResponse newVehicle = vehicleService.addVehicle(request);
    return new ResponseEntity<>(newVehicle, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update vehicle details")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable Long id, @ModelAttribute VehicleUpdateRequest request) {
    VehicleResponse updated = vehicleService.updateVehicle(id, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a vehicle")
  public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
    vehicleService.deleteVehicle(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get all vehicles")
  public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
    return ResponseEntity.ok(vehicleService.findAll());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get vehicle by ID")
  public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleService.findById(id));
  }

    @GetMapping("/search")
    @Operation(summary = "Search available vehicles (Customer)")
    public ResponseEntity<List<VehicleResponse>> searchVehicles(
            @RequestParam Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Validate ngày tháng chút cho chắc
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            // throw new IllegalArgumentException("Start date cannot be in the past"); // Tùy logic
        }

        return ResponseEntity.ok(vehicleService.searchAvailableVehicles(stationId, startDate, endDate));
    }

  // API này phụ thuộc vào BookingService (Em nhớ làm phần Booking tiếp theo nhé)
  @GetMapping("/{vehicleId}/active-bookings")
  @Operation(summary = "Get active bookings for a vehicle")
  public ResponseEntity<List<BookingResponse>> get3OnGoingBookings(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(bookingService.get3OnGoingBookingsOfVehicle(vehicleId));
  }

  @GetMapping("/search-by-location")
  @Operation(summary = "Search vehicles by location (city, district, ward)")
  public ResponseEntity<List<VehicleResponse>> searchVehiclesByLocation(
          @RequestParam Long locationId) {
    return ResponseEntity.ok(vehicleService.findVehiclesByLocation(locationId));
  }

  @GetMapping("/search-by-model")
  @Operation(summary = "Search vehicles by model name or brand")
  public ResponseEntity<List<VehicleResponse>> searchVehiclesByModel(
          @RequestParam String modelName) {
    return ResponseEntity.ok(vehicleService.findVehiclesByModel(modelName));
  }

  @GetMapping("/search-by-name")
  @Operation(summary = "Search vehicles by license plate or model name")
  public ResponseEntity<List<VehicleResponse>> searchVehiclesByName(
          @RequestParam String query) {
    return ResponseEntity.ok(vehicleService.findVehiclesByName(query));
  }
}

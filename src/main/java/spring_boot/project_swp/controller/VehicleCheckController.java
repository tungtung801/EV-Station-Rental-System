package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;
import spring_boot.project_swp.service.VehicleCheckService;

@RestController
@RequestMapping("/api/vehiclechecks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Vehicle Check APIs", description = "APIs for managing vehicle checks")
public class VehicleCheckController {

  final VehicleCheckService vehicleCheckService;

  @PostMapping(consumes = {"multipart/form-data"})
  @Operation(
      summary = "Create a new vehicle check",
      description = "Creates a new vehicle check record with image uploads. Staff ID is auto-detected from JWT token, Check Type is auto-detected based on rental stage.")
  public ResponseEntity<VehicleCheckResponse> createVehicleCheck(
      @AuthenticationPrincipal String email,
      @RequestParam Long rentalId,
      @RequestParam(required = false) String notes,
      @RequestParam(required = false) List<MultipartFile> images) {

    VehicleCheckRequest request = VehicleCheckRequest.builder()
        .rentalId(rentalId)
        .notes(notes)
        .images(images)
        .build();

    return new ResponseEntity<>(
        vehicleCheckService.createVehicleCheck(email, request), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get vehicle check by ID",
      description = "Retrieves a vehicle check record by its unique ID.")
  public ResponseEntity<VehicleCheckResponse> getVehicleCheckById(@PathVariable Long id) {
    return new ResponseEntity<>(vehicleCheckService.getVehicleCheckById(id), HttpStatus.OK);
  }

  @GetMapping
  @Operation(
      summary = "Get all vehicle checks",
      description = "Retrieves a list of all vehicle check records.")
  public ResponseEntity<List<VehicleCheckResponse>> getAllVehicleChecks() {
    return new ResponseEntity<>(vehicleCheckService.getAllVehicleChecks(), HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update vehicle check",
      description = "Updates an existing vehicle check record.")
  public ResponseEntity<VehicleCheckResponse> updateVehicleCheck(
      @PathVariable Long id,
      @AuthenticationPrincipal String email,
      @RequestParam Long rentalId,
      @RequestParam(required = false) String notes,
      @RequestParam(required = false) List<MultipartFile> images) {

    VehicleCheckRequest request = VehicleCheckRequest.builder()
        .rentalId(rentalId)
        .notes(notes)
        .images(images)
        .build();

    return new ResponseEntity<>(vehicleCheckService.updateVehicleCheck(id, email, request), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete vehicle check",
      description = "Deletes a vehicle check record by its unique ID.")
  public ResponseEntity<Void> deleteVehicleCheck(@PathVariable Long id) {
    vehicleCheckService.deleteVehicleCheck(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

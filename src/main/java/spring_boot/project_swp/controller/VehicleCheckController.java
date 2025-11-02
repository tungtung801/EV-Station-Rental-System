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

  @PostMapping
  @Operation(
      summary = "Create a new vehicle check",
      description = "Creates a new vehicle check record.")
  public ResponseEntity<VehicleCheckResponse> createVehicleCheck(
      @RequestBody @Valid VehicleCheckRequest request) {
    return new ResponseEntity<>(
        vehicleCheckService.createVehicleCheck(request), HttpStatus.CREATED);
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
      @PathVariable Long id, @RequestBody @Valid VehicleCheckRequest request) {
    return new ResponseEntity<>(vehicleCheckService.updateVehicleCheck(id, request), HttpStatus.OK);
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

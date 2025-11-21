package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor; // <--- SỬA
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.service.VehicleModelService;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor // <--- Dùng cái này
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Vehicle Model APIs", description = "APIs for managing vehicle models")
public class VehicleModelController {

  final VehicleModelService vehicleModelService;

  @GetMapping
  @Operation(summary = "Get all vehicle models")
  public ResponseEntity<List<VehicleModelResponse>> getAllModels() {
    return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get vehicle model by ID")
  public ResponseEntity<VehicleModelResponse> getModelById(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleModelService.getVehicleModelById(id));
  }

  @PostMapping
  @Operation(summary = "Add a new vehicle model")
  public ResponseEntity<VehicleModelResponse> addVehicleModel(
      @Valid @RequestBody VehicleModelRequest request) {
    return new ResponseEntity<>(vehicleModelService.addVehicleModel(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update vehicle model")
  public ResponseEntity<VehicleModelResponse> updateVehicleModel(
      @PathVariable Long id, @Valid @RequestBody VehicleModelRequest request) {
    return new ResponseEntity<>(vehicleModelService.updateVehicleModel(id, request), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete vehicle model")
  public ResponseEntity<Void> deleteVehicleModel(@PathVariable Long id) {
    vehicleModelService.deleteVehicleModel(id);
    return ResponseEntity.noContent().build();
  }
}

package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.service.VehicleModelService;

@RestController
@RequestMapping("/api/vehicle-models")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Vehicle Model APIs", description = "APIs for managing vehicle models")
public class VehicleModelController {
  final VehicleModelService vehicleModelService;

  @GetMapping
  @Operation(
      summary = "Get all vehicle models",
      description = "Retrieves a list of all vehicle models.")
  public ResponseEntity<List<VehicleModelResponse>> getAllModels() {
    return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get vehicle model by ID",
      description = "Retrieves a vehicle model by its unique ID.")
  public ResponseEntity<VehicleModelResponse> getModelById(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleModelService.getVehicleModelById(id));
  }

  @PostMapping
  @Operation(
      summary = "Add a new vehicle model",
      description = "Adds a new vehicle model to the system.")
  public ResponseEntity<VehicleModelResponse> addVehicleModel(
      @Valid @RequestBody VehicleModelRequest request) {
    return new ResponseEntity<>(vehicleModelService.addVehicleModel(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update vehicle model",
      description = "Updates an existing vehicle model's information.")
  public ResponseEntity<VehicleModelResponse> updateVehicleModel(
      @PathVariable Long id, @Valid @RequestBody VehicleModelRequest request) {
    return ResponseEntity.ok(vehicleModelService.updateVehicleModel(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete vehicle model",
      description = "Deletes a vehicle model by its unique ID.")
  public ResponseEntity<Void> deleteVehicleModel(@PathVariable Long id) {
    vehicleModelService.deleteVehicleModel(id);
    return ResponseEntity.noContent().build();
  }
}

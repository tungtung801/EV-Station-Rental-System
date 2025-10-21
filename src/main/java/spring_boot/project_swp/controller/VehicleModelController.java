package spring_boot.project_swp.controller;

import java.util.List;

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

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.service.VehicleModelService;

@RestController
@RequestMapping("/api/vehicle-models")
@AllArgsConstructor
public class VehicleModelController {
    private final VehicleModelService vehicleModelService;

    //------------ Get All Vehicle Models ----------
    @GetMapping
    public ResponseEntity<List<VehicleModelResponse>> getAllModels() {
        return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
    }

    //------------ Get Vehicle Model by ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<VehicleModelResponse> getModelById(@PathVariable int id) {
        return ResponseEntity.ok(vehicleModelService.getVehicleModelById(id));
    }

    //------------ Add Vehicle Model ----------
    @PostMapping
    public ResponseEntity<VehicleModelResponse> addVehicleModel(@Valid @RequestBody VehicleModelRequest request) {
        return new ResponseEntity<>(vehicleModelService.addVehicleModel(request), HttpStatus.CREATED);
    }

    //------------ Update Vehicle Model ----------
    @PutMapping("/{id}")
    public ResponseEntity<VehicleModelResponse> updateVehicleModel(@PathVariable int id, @Valid @RequestBody VehicleModelRequest request) {
        return ResponseEntity.ok(vehicleModelService.updateVehicleModel(id, request));
    }

    //------------ Delete Vehicle Model ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleModel(@PathVariable int id) {
        vehicleModelService.deleteVehicleModel(id);
        return ResponseEntity.noContent().build();
    }
}

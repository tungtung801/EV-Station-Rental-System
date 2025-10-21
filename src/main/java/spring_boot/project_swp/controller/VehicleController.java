package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@AllArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    //------------ Create Vehicle ----------
    @PostMapping("/create")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse newVehicle = vehicleService.addVehicle(request);
        return new ResponseEntity<>(newVehicle, HttpStatus.CREATED);
    }

    //------------ Update Vehicle ----------
    @PutMapping("/update/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable int id, @Valid @RequestBody VehicleRequest request) {
        VehicleResponse updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(updated);
    }

    //------------ Delete Vehicle ----------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable int id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    //------------ Get All Vehicles ----------
    @GetMapping("/all")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.findAll());
    }

    //------------ Get Vehicle by ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable int id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }
}

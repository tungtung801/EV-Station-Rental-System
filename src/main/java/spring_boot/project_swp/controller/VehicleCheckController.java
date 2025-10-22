package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;
import spring_boot.project_swp.service.VehicleCheckService;

import java.util.List;

@RestController
@RequestMapping("/api/vehiclechecks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleCheckController {

    final VehicleCheckService vehicleCheckService;

    @PostMapping
    public ResponseEntity<VehicleCheckResponse> createVehicleCheck(@RequestBody @Valid VehicleCheckRequest request) {
        return new ResponseEntity<>(vehicleCheckService.createVehicleCheck(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleCheckResponse> getVehicleCheckById(@PathVariable Long id) {
        return new ResponseEntity<>(vehicleCheckService.getVehicleCheckById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<VehicleCheckResponse>> getAllVehicleChecks() {
        return new ResponseEntity<>(vehicleCheckService.getAllVehicleChecks(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleCheckResponse> updateVehicleCheck(@PathVariable Long id, @RequestBody @Valid VehicleCheckRequest request) {
        return new ResponseEntity<>(vehicleCheckService.updateVehicleCheck(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleCheck(@PathVariable Long id) {
        vehicleCheckService.deleteVehicleCheck(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
package spring_boot.project_swp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.service.VehicleModelService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-models")
@AllArgsConstructor
public class VehicleModelController {
    private final VehicleModelService vehicleModelService;

    @GetMapping("/all")
    public ResponseEntity<List<VehicleModel>> getAllModels() {
        return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleModel> getModelById(@PathVariable int id) {
        VehicleModel model = vehicleModelService.getVehicleModelById(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(model);
    }

    @PostMapping("/add")
    public ResponseEntity<VehicleModel> addVehicleModel(@RequestBody VehicleModel vehicleModel) {
        boolean added = vehicleModelService.addVehicleModel(vehicleModel);
        if (added) {
            return new ResponseEntity<>(vehicleModel, HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

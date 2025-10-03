package spring_boot.project_swp.controller;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.service.LocationService;

@RestController
@RequestMapping("/api/locations")
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {
    LocationService locationService;

    @PostMapping("/add")
    public ResponseEntity<?> addLocation(@RequestBody LocationAddingRequest request) {
        return new ResponseEntity<>(locationService.addLocation(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable Integer id, @RequestBody LocationUpdateRequest request) {
        return new ResponseEntity<>(locationService.updateLocation(id, request), HttpStatus.OK);
    }

    @GetMapping("/getById/{locationId}")
    public ResponseEntity<?> getLocationById(@PathVariable Integer locationId) {
        return new ResponseEntity<>(locationService.getLocationById(locationId), HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllLocations() {
        return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
    }
}

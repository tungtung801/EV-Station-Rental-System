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
@RequestMapping("/api/location")
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {
    LocationService locationService;

    //------------ Add Location ----------
    @PostMapping("/add")
    public ResponseEntity<?> addLocation(@RequestBody LocationAddingRequest request) {
        return new ResponseEntity<>(locationService.addLocation(request), HttpStatus.CREATED);
    }

    //------------ Update Location ----------
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable int id, @RequestBody LocationUpdateRequest request) {
        return new ResponseEntity<>(locationService.updateLocation(id, request), HttpStatus.OK);
    }

    //------------ Get Location by ID ----------
    @GetMapping("/get/getById/{locationId}")
    public ResponseEntity<?> getLocationById(@PathVariable Integer locationId) {
        return new ResponseEntity<>(locationService.getLocationById(locationId), HttpStatus.OK);
    }

    //------------ Get All Locations ----------
    @GetMapping("/get/getAll")
    public ResponseEntity<?> getAllLocations() {
        return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
    }

    //------------ Get Cities ----------
    @GetMapping("/get/getCities")
    public ResponseEntity<?> getCities() {
        return new ResponseEntity<>(locationService.getCities(), HttpStatus.OK);
    }

    //------------ Get Districts by City ID ----------
    @GetMapping("/get/getDistricts/{cityId}")
    public ResponseEntity<?> getDistricts(@PathVariable Integer cityId) {
        return new ResponseEntity<>(locationService.getDistrictsByCityId(cityId), HttpStatus.OK);
    }

    //------------ Get Wards by District ID ----------
    @GetMapping("/get/getWards/{districtId}")
    public ResponseEntity<?> getWards(@PathVariable Integer districtId) {
        return new ResponseEntity<>(locationService.getWardByDistrictId(districtId), HttpStatus.OK);
    }

    //------------ Delete Location ----------
    @PostMapping("/delete/{locationId}")
    public ResponseEntity<?> deleteLocation(@PathVariable int locationId) {
        return new ResponseEntity<>(locationService.deleteLocation(locationId), HttpStatus.OK);
    }

}

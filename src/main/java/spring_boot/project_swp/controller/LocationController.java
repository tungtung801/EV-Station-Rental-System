package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.service.LocationService;

@RestController
@RequestMapping("/api/location")
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Location APIs", description = "APIs for managing locations (cities, districts, wards)")
public class LocationController {
  LocationService locationService;

  @PostMapping
  @Operation(
      summary = "Add a new location",
      description = "Adds a new location (city, district, or ward) to the system.")
  public ResponseEntity<LocationResponse> addLocation(
      @RequestBody @Valid LocationAddingRequest request) {
    return new ResponseEntity<>(locationService.addLocation(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update an existing location",
      description = "Updates an existing location's details.")
  public ResponseEntity<LocationResponse> updateLocation(
      @PathVariable Long id, @RequestBody @Valid LocationUpdateRequest request) {
    return new ResponseEntity<>(locationService.updateLocation(id, request), HttpStatus.OK);
  }

  @GetMapping("/{locationId}")
  @Operation(summary = "Get location by ID", description = "Retrieves a location by its unique ID.")
  public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long locationId) {
    return new ResponseEntity<>(locationService.getLocationById(locationId), HttpStatus.OK);
  }

  @GetMapping
  @Operation(summary = "Get all locations", description = "Retrieves a list of all locations.")
  public ResponseEntity<List<LocationResponse>> getAllLocations() {
    return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
  }

  @GetMapping("/getCities")
  @Operation(summary = "Get all cities", description = "Retrieves a list of all cities.")
  public ResponseEntity<List<LocationResponse>> getCities() {
    return new ResponseEntity<>(locationService.getCities(), HttpStatus.OK);
  }

  @GetMapping("/getDistricts/{cityId}")
  @Operation(
      summary = "Get districts by city ID",
      description = "Retrieves a list of districts for a given city ID.")
  public ResponseEntity<List<LocationResponse>> getDistricts(@PathVariable Long cityId) {
    return new ResponseEntity<>(locationService.getDistrictsByCityId(cityId), HttpStatus.OK);
  }

  @GetMapping("/getWards/{districtId}")
  @Operation(
      summary = "Get wards by district ID",
      description = "Retrieves a list of wards for a given district ID.")
  public ResponseEntity<List<LocationResponse>> getWards(@PathVariable Long districtId) {
    return new ResponseEntity<>(locationService.getWardByDistrictId(districtId), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a location", description = "Deletes a location by its ID.")
  public ResponseEntity<Void> deleteLocation(@PathVariable Long locationId) {
    locationService.deleteLocation(locationId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

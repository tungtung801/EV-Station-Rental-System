package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.StationResponse;
import spring_boot.project_swp.service.StationService;

@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
@Tag(name = "Station APIs", description = "APIs for managing vehicle stations")
public class StationController {
  private final StationService stationService;

  @PostMapping
  @Operation(summary = "Add a new station", description = "Adds a new vehicle station.")
  public ResponseEntity<StationResponse> addStation(
      @Valid @RequestBody StationAddingRequest station) {
    return new ResponseEntity<>(stationService.addStation(station), HttpStatus.CREATED);
  }

  @GetMapping
  @Operation(
      summary = "Get all stations",
      description = "Retrieves a list of all vehicle stations.")
  public ResponseEntity<List<StationResponse>> getAllStations() {
    return new ResponseEntity<>(stationService.getAllStations(), HttpStatus.OK);
  }

  @GetMapping("/location/{locationId}")
  @Operation(
      summary = "Get stations by location ID",
      description = "Retrieves a list of stations for a specific location.")
  public ResponseEntity<List<StationResponse>> getAllStationsByLocationId(
      @PathVariable("locationId") Long locationId) {
    return new ResponseEntity<>(
        stationService.getAllStationsByLocationId(locationId), HttpStatus.OK);
  }

  @GetMapping("/city/{cityId}")
  @Operation(
      summary = "Get stations by city ID",
      description = "Retrieves a list of stations for a specific city.")
  public ResponseEntity<List<StationResponse>> getStationsByCityId(
      @PathVariable("cityId") Long cityId) {
    return new ResponseEntity<>(stationService.findStationsByCityId(cityId), HttpStatus.OK);
  }

  @GetMapping("/city/{cityId}/district/{districtId}")
  @Operation(
      summary = "Get stations by city and district ID",
      description = "Retrieves a list of stations for a specific city and district.")
  public ResponseEntity<List<StationResponse>> getStationsByDistrictId(
      @PathVariable("cityId") Long cityId, @PathVariable("districtId") Long districtId) {
    return new ResponseEntity<>(
        stationService.findStationsByDistrictId(cityId, districtId), HttpStatus.OK);
  }

  @PutMapping("/{stationId}")
  @Operation(
      summary = "Update an existing station",
      description = "Updates an existing station's details.")
  public ResponseEntity<StationResponse> updateStation(
      @PathVariable("stationId") Long stationId, @RequestBody @Valid StationUpdateRequest station) {
    return new ResponseEntity<>(stationService.updateStation(stationId, station), HttpStatus.OK);
  }

  @DeleteMapping("/{stationId}")
  @Operation(summary = "Delete a station", description = "Deletes a station by its ID.")
  public ResponseEntity<Void> deleteStationById(@PathVariable Long stationId) {
    stationService.deleteStationById(stationId);
    return ResponseEntity.noContent().build();
  }
}

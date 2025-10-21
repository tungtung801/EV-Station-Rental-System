package spring_boot.project_swp.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.MessageResponse;
import spring_boot.project_swp.dto.response.StationResponse;
import spring_boot.project_swp.service.StationService;

@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
public class StationController {
    private final StationService stationService;

    @PostMapping("/add")
    public ResponseEntity<StationResponse> addStation(@Valid @RequestBody StationAddingRequest station) {
        return new ResponseEntity<>(stationService.addStation(station),  HttpStatus.OK);
    }

    @GetMapping("/get/getAll")
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return new ResponseEntity<>(stationService.getAllStations(),  HttpStatus.OK);
    }

    @GetMapping("/get/getStationByLocation/{locationId}")
    public ResponseEntity<List<StationResponse>> getAllStationsByLocationId(@PathVariable("locationId") Integer locationId) {
        return new ResponseEntity<>(stationService.getAllStationsByLocationId(locationId),  HttpStatus.OK);
    }

    @GetMapping("/search/by-city/{cityId}")
    public ResponseEntity<List<StationResponse>> getStationsByCityId(@PathVariable("cityId") Integer cityId) {
        return new ResponseEntity<>(stationService.findStationsByCityId(cityId),  HttpStatus.OK);
    }

    @GetMapping("/search/by-city/{cityId}/by-district/{districtId}")
    public ResponseEntity<List<StationResponse>> getStationsByDistrictId(@PathVariable("cityId") Integer cityId
    , @PathVariable("districtId") Integer districtId) {
        return new ResponseEntity<>(stationService.findStationsByDistrictId(cityId, districtId),  HttpStatus.OK);
    }

    @PostMapping("/update/{stationId}")
    public ResponseEntity<StationResponse> updateStation(@PathVariable("stationId") Integer stationId, @RequestBody StationUpdateRequest station) {
        return new ResponseEntity<>(stationService.updateStation(stationId, station),  HttpStatus.OK);
    }

    @DeleteMapping("/{stationId}")
    public ResponseEntity<Void> deleteStationById(@PathVariable Integer stationId) {
        stationService.deleteStationById(stationId);
        return ResponseEntity.noContent().build();
    }
}

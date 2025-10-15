package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.service.StationService;

@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
public class StationController {
    private final StationService stationService;

    @PostMapping("/add")
    public ResponseEntity<?> addStation(@Valid @RequestBody StationAddingRequest station) {
        return new ResponseEntity<>(stationService.addStation(station),  HttpStatus.OK);
    }

    @GetMapping("/get/getAll")
    public ResponseEntity<?> getAllStations() {
        return new ResponseEntity<>(stationService.getAllStations(),  HttpStatus.OK);
    }

    @GetMapping("/get/getStationByLocation/{locationId}")
    public ResponseEntity<?> getAllStationsByLocationId(@PathVariable("locationId") Integer locationId) {
        return new ResponseEntity<>(stationService.getAllStationsByLocationId(locationId),  HttpStatus.OK);
    }

    @GetMapping("/search/by-city/{cityId}")
    public ResponseEntity<?> getStationsByCityId(@PathVariable("cityId") Integer cityId) {
        return new ResponseEntity<>(stationService.findStationsByCityId(cityId),  HttpStatus.OK);
    }

    @GetMapping("/search/by-city/{cityId}/by-district/{districtId}")
    public ResponseEntity<?> getStationsByDistrictId(@PathVariable("cityId") Integer cityId
    , @PathVariable("districtId") Integer districtId) {
        return new ResponseEntity<>(stationService.findStationsByDistrictId(cityId, districtId),  HttpStatus.OK);
    }

    @PostMapping("/update/{stationId}")
    public ResponseEntity<?> updateStation(@PathVariable("stationId") Integer stationId, @RequestBody StationUpdateRequest station) {
        return new ResponseEntity<>(stationService.updateStation(stationId, station),  HttpStatus.OK);
    }

    @PostMapping("/delete/{stationId}")
    public ResponseEntity<?> deleteStation(@PathVariable Integer stationId){
        return new ResponseEntity<>(stationService.deleteStationById(stationId), HttpStatus.OK);
    }
}

package spring_boot.project_swp.service.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.StationResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.StationStatusEnum;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.LocationMapper;
import spring_boot.project_swp.mapper.StationMapper;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.StationService;

@Service
@RequiredArgsConstructor
@Transactional
public class StationServiceImpl implements StationService {
  private final StationRepository stationRepository;
  private final StationMapper stationMapper;
  private final LocationRepository locationRepository;
  private final LocationMapper locationMapper;
  private final RentalRepository rentalRepository;
  private final VehicleRepository vehicleRepository;

  @Override
  public List<StationResponse> getAllStations() {
    List<Station> activeStations = stationRepository.findByIsActive(StationStatusEnum.ACTIVE);
    List<StationResponse> stationResponses = new ArrayList<>();
    for (Station station : activeStations) {
      stationResponses.add(stationMapper.toStationResponse(station));
    }
    return stationResponses;
  }

  @Override
  public List<StationResponse> getAllStationsByLocationId(Long locationId) {
    List<Station> stationByLocaitonList =
        stationRepository.findByLocation_LocationIdAndIsActive(
            locationId, StationStatusEnum.ACTIVE);
    return stationMapper.toStationResponseList(stationByLocaitonList);
  }

  @Override
  public StationResponse findStationById(Long id) {
    if (id == null) {
      throw new ConflictException("StationID is required");
    }
    Station station =
        stationRepository
            .findStationByStationId(id)
            .orElseThrow(() -> new NotFoundException("Station does not exist"));
    return stationMapper.toStationResponse(station);
  }

  @Override
  public StationResponse findStationByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new ConflictException("StationName is required");
    }
    Station station =
        stationRepository
            .findStationByStationName(name)
            .orElseThrow(() -> new NotFoundException("Station does not exist"));
    return stationMapper.toStationResponse(station);
  }

  @Override
  public StationResponse addStation(StationAddingRequest request) {
    if (request.getStationName() == null || request.getStationName().trim().isEmpty()) {
      throw new ConflictException("StationName is required");
    }

    if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
      throw new ConflictException("Address is required");
    }

    if (stationRepository.findStationByStationName(request.getStationName()).isPresent()) {
      throw new ConflictException("Station already exists");
    }

    Station newStation = stationMapper.toStation(request);

    if (request.getIsActive() != null) {
      newStation.setIsActive(request.getIsActive());
    } else {
      newStation.setIsActive(StationStatusEnum.ACTIVE);
    }

    newStation.setAvailableDocks(request.getTotalDocks());

    Location location =
        locationRepository
            .findById(request.getLocationId())
            .orElseThrow(
                () ->
                    new NotFoundException("Location not found with id " + request.getLocationId()));
    newStation.setLocation(location);

    stationRepository.save(newStation);

    return stationMapper.toStationResponse(newStation);
  }

  @Override
  public StationResponse updateStation(Long stationId, StationUpdateRequest request) {
    Station station =
        stationRepository
            .findStationByStationId(stationId)
            .orElseThrow(() -> new NotFoundException("Station does not exist"));

    stationMapper.updateStationFromRequest(request, station);

    if (request.getLocationId() != null) {
      Location newLocation =
          locationRepository
              .findByLocationId(request.getLocationId())
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          "Location does not exist with id: " + request.getLocationId()));
      station.setLocation(newLocation);
    }

    Station updatedStation = stationRepository.save(station);

    return stationMapper.toStationResponse(updatedStation);
  }

  @Override
  public void deleteStationById(Long id) {
    Station station =
        stationRepository
            .findStationByStationId(id)
            .orElseThrow(() -> new NotFoundException("Station does not exist"));

    // Delete all related rentals first
    rentalRepository.deleteAll(station.getPickupRentals());
    rentalRepository.deleteAll(station.getReturnRentals());

    // Delete all related vehicles
    vehicleRepository.deleteAll(station.getVehicles());

    // 3) Detach from parent Location to avoid being re-persisted by Location.stations cascade
    Location location = station.getLocation();
    if (location != null && location.getStations() != null) {
      location.getStations().removeIf(s -> s.getStationId() != null && s.getStationId().equals(id));
    }
    station.setLocation(null);

    // 4) Hard delete the Station; JPA will cascade remove remaining dependents (e.g., Vehicles)
    stationRepository.delete(station);
  }

  @Override
  public List<StationResponse> findStationsByCityId(Long cityId) {
    if (cityId == null) {
      throw new ConflictException("CityID is required");
    }
    Location city =
        locationRepository
            .findByLocationId(cityId)
            .orElseThrow(
                () -> new NotFoundException("CityID does not exist or Invalid type name \"City\""));
    if (!city.getLocationType().equalsIgnoreCase("City")) {
      throw new NotFoundException("CityID does not exist or Invalid type name \"City\"");
    }

    Set<Long> locationIds = new HashSet<>();
    locationIds.add(cityId);
    collectChildrenIds(city, locationIds);

    List<Station> stations =
        stationRepository.findByLocation_LocationIdInAndIsActive(
            new ArrayList<>(locationIds), StationStatusEnum.ACTIVE);
    return stationMapper.toStationResponseList(stations);
  }

  @Override
  public List<StationResponse> findStationsByDistrictId(Long cityId, Long districtId) {
    if (cityId == null || districtId == null) {
      throw new ConflictException("CityID or districtID is missing");
    }
    Location district =
        locationRepository
            .findByLocationId(districtId)
            .orElseThrow(
                () -> new NotFoundException("The district is not belongs to the specificed city"));
    if (!district.getParent().getLocationId().equals(cityId)) {
      throw new NotFoundException("The district is not belongs to the specificed city");
    }

    Set<Long> locationIds = new HashSet<>();
    locationIds.add(districtId);
    collectChildrenIds(district, locationIds);

    List<Station> stations =
        stationRepository.findByLocation_LocationIdInAndIsActive(
            new ArrayList<>(locationIds), StationStatusEnum.ACTIVE);
    return stationMapper.toStationResponseList(stations);
  }

  private void collectChildrenIds(Location location, Set<Long> collectedIds) {
    if (location.getChildren() != null && !location.getChildren().isEmpty()) {
      for (Location child : location.getChildren()) {
        collectedIds.add(child.getLocationId());
        collectChildrenIds(child, collectedIds);
      }
    }
  }
}

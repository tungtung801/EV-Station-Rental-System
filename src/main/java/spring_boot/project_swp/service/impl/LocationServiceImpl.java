package spring_boot.project_swp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.respone.LocationResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.mapper.LocationMapper;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.service.LocationService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public Location addLocation(LocationAddingRequest request) {
        if (request.getLocationName() == null || request.getLocationName().trim().isEmpty()) {
            throw new IllegalArgumentException("LocationName is required");
        }
        if (request.getLocationType() == null || request.getLocationType().trim().isEmpty()) {
            throw new IllegalArgumentException("LocationType is required");
        }

        if (locationRepository.findByLocationName(request.getLocationName()) != null) {
            throw new IllegalArgumentException("Location already exists");
        }

        Location newLocation = locationMapper.toLocation(request);
        newLocation.setActive(true);
        newLocation.setCreatedAt(LocalDate.now());
        return locationRepository.save(newLocation);
    }

    @Override
    // luong hoat dọng:
    // Client -> Controller 1 json -> mapstruct map json đó về obj -> update các field cần thiet, update luon ca parent -> map vè lại responseDTO trả về -> Client chuỗi json
    public LocationResponse updateLocation(int locationId, LocationUpdateRequest request) {
        Location existtingLocation = getLocationById(locationId);
        if (existtingLocation == null) {
            throw new IllegalArgumentException("Location does not exist");
        }
        // Lay thong tin cap nhat moi tu request roi map vao location hien tai dang can update thong qua mapper voi locationId
        locationMapper.updateLocationFromRequest(request, existtingLocation);

        //xu li parent
        if(request.getParentLocationId() != null) {
            Location parentLocation = getLocationById(request.getParentLocationId());
            if (parentLocation == null) {
                throw new IllegalArgumentException("Parent location does not exist");
            }
            existtingLocation.setParent(parentLocation);
        }
        Location updatedLocation = locationRepository.save(existtingLocation);
        return locationMapper.toLocationResponse(updatedLocation);
    }


    @Override
    public Location getLocationById(int locationId) {
        return locationRepository.findById(locationId).get();
    }


    @Override
    public Location getLocationByName(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new IllegalArgumentException("LocationName is required");
        }
        return null;
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public void deleteLocation(int locationId) {

    }

    @Override
    public Location addParentLocation(int parentId, Location location) {
        return null;
    }
}

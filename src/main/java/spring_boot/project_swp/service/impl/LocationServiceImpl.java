package spring_boot.project_swp.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.respone.LocationUpdateResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.service.LocationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {

    @Autowired
    LocationRepository locationRepository;

    @Override
    public Location addLocation(LocationAddingRequest location) {
        Optional<Location> existLocation = locationRepository.findByLocationName(location.getLocationName());
        if (existLocation.isPresent()) {
            return null;
        } else {
            Location newLocation = new Location();
            newLocation.setLocationName(location.getLocationName());
            newLocation.setLocationType(location.getLocationType());
            newLocation.setActive(true);
            newLocation.setCreatedAt(LocalDate.now());
            return locationRepository.save(newLocation);
        }
    }

    @Override
    public LocationUpdateResponse updateLocation(Integer locationId, LocationUpdateRequest request) {
        Location existLocation = locationRepository.findByLocationId(locationId).orElseThrow(()
                -> new RuntimeException("Location not found"));
        existLocation.setLocationName(request.getLocationName());
        existLocation.setLocationType(request.getLocationType());
        existLocation.setAddress(request.getAddress());
        existLocation.setLatitude(request.getLatitude());
        existLocation.setLongitude(request.getLongitude());
        existLocation.setRadius(request.getRadius());

        Integer parentLocationId = request.getParentLocationId();
        if (parentLocationId.equals(locationId)) {
            throw new RuntimeException("A location cannot be its own parent.");
        } else {
            Location newParentLocation = locationRepository.findByLocationId(parentLocationId).orElseThrow(()
                    -> new RuntimeException("Parent location not found"));
            existLocation.setParent(newParentLocation);
        }

        Location updatedLocation = locationRepository.save(existLocation);
        LocationUpdateResponse response = new LocationUpdateResponse();
        response.setLocationId(updatedLocation.getLocationId());
        response.setLocationName(updatedLocation.getLocationName());
        response.setLocationType(updatedLocation.getLocationType());
        response.setAddress(updatedLocation.getAddress());
        response.setLatitude(updatedLocation.getLatitude());
        response.setLongitude(updatedLocation.getLongitude());
        response.setRadius(updatedLocation.getRadius());
        if (updatedLocation.getParent() != null) {
            response.setParentLocationId(updatedLocation.getParent().getLocationId());
        } else {
            response.setParentLocationId(null);
        }

        return response;
    }


    @Override
    public Optional<Location> getLocationById(Integer id) {
        return locationRepository.findByLocationId(id);
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public void deleteLocation(Integer locationId) {

    }

    @Override
    public Location addParentLocation(int parentId, Location location) {
        return null;
    }
}

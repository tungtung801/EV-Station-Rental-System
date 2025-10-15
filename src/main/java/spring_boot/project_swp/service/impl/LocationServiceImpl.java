package spring_boot.project_swp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.mapper.LocationMapper;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.service.LocationService;

import java.time.LocalDate;
import java.util.ArrayList;
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

        // ====== LƯU Ý: TYPE GHI TIENG ANH (City / District / Ward, ... )
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
        if (request.getParentLocationId() != null) {
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
        return locationRepository.findById(locationId).orElseThrow(() -> new IllegalArgumentException("Location does not exist"));
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
    public List<Location> getAllLocationsIsActiveTrue() {
        return locationRepository.findByIsActiveIsTrue();
    }

    @Override
    public LocationResponse deleteLocation(int locationId) {
        Location location = getLocationById(locationId);
        if (location != null) {
            if (location.getLocationType().equalsIgnoreCase("Thành phố")) {
                // Phải xóa các location con ph thuộc trước
                List<Location> children = getChildLocation(location);

                if (children != null) {
                    for (Location child : children) {
                        child.setActive(false);
                        locationRepository.save(child); // soft delete cho con.
                    }

                    location.setActive(false);
                    locationRepository.save(location);
                }
            } else {
                List<Location> children = getChildLocation(location);

                if (children.isEmpty()) {
                    locationRepository.delete(location); // neu dang la location cha va ko co con thi xoa thang
                }
            }
        }
        return locationMapper.toLocationResponse(location);
    }

    @Override
    public List<Location> getChildLocation(Location location) {
        List<Location> childLocations = new ArrayList<>();
        for (Location child : location.getChildren()) {
            childLocations.add(child);
        }
        return childLocations;
    }

    // ====== PHỤC VỤ TÍNH NĂNG SEARCH CHO NGƯỜI DÙNG ==================================
    // ====== LƯU Ý: TYPE GHI TIENG ANH (City / District / Ward, ... )
    @Override
    public List<Location> getCities() {
        return locationRepository.findByLocationTypeAndIsActiveTrueOrderByLocationNameAsc("City");
    }

    @Override
    public List<Location> getDistrictsByCityId(Integer cityId) {
        if(cityId == null){
            throw new IllegalArgumentException("CityId is required for filtering");
        }
        return locationRepository.findByParent_LocationIdAndLocationTypeAndIsActiveTrueOrderByLocationNameAsc(cityId,  "District");
    }

    @Override
    public List<Location> getWardByDistrictId(Integer districtId) {
        if(districtId == null){
            throw new IllegalArgumentException("DistrictId is required for filtering");
        }
        return locationRepository.findByParent_LocationIdAndLocationTypeAndIsActiveTrueOrderByLocationNameAsc(districtId, "Ward");
    }
}
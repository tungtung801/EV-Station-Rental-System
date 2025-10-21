package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;

import java.util.List;

@Service
public interface LocationService {
    public LocationResponse addLocation(LocationAddingRequest request);

    public LocationResponse updateLocation(int locationId, LocationUpdateRequest location);

    public LocationResponse getLocationById(int locationId);

    public LocationResponse getLocationByName(String locationName);

    public List<LocationResponse> getAllLocations();

    public List<LocationResponse> getAllLocationsIsActiveTrue();

    public LocationResponse deleteLocation(int locationId);

    public List<LocationResponse> getChildLocation(Location location);

    public List<LocationResponse> getCities();

    public List<LocationResponse> getDistrictsByCityId(Integer cityId);

    public List<LocationResponse> getWardByDistrictId(Integer districtId);

}

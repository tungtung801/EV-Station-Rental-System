package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;

import java.util.List;

@Service
public interface LocationService {
    public Location addLocation(LocationAddingRequest request);

    public LocationResponse updateLocation(int locationId, LocationUpdateRequest location);

    public Location getLocationById(int locationId);

    public Location getLocationByName(String locationName);

    public List<Location> getAllLocations();

    public List<Location> getAllLocationsIsActiveTrue();

    public LocationResponse deleteLocation(int locationId);

    public List<Location> getChildLocation(Location location);

    public List<Location> getCities();

    public List<Location> getDistrictsByCityId(Integer cityId);

    public List<Location> getWardByDistrictId(Integer districtId);

}

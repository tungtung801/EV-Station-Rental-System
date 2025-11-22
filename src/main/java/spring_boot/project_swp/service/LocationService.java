package spring_boot.project_swp.service;

import java.util.List;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;

@Service
public interface LocationService {
  public LocationResponse addLocation(LocationAddingRequest request);

  public LocationResponse updateLocation(Long locationId, LocationUpdateRequest location);

  public LocationResponse getLocationById(Long locationId);

  public LocationResponse getLocationByName(String locationName);

  public List<LocationResponse> getAllLocations();

  public List<LocationResponse> getAllLocationsIsActiveTrue();

  public LocationResponse deleteLocation(Long locationId);

  public List<LocationResponse> getChildLocation(Location location);

  public List<LocationResponse> getCities();

  public List<LocationResponse> getDistrictsByCityId(Long cityId);

  public List<LocationResponse> getWardByDistrictId(Long districtId);

  public Location findAndParseLocationFromAddress(String address);
}

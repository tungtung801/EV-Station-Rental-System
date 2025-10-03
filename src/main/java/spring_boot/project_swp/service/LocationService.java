package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.respone.LocationUpdateResponse;
import spring_boot.project_swp.entity.Location;

import java.util.List;
import java.util.Optional;

@Service
public interface LocationService {
    public Location addLocation(LocationAddingRequest request);

    public LocationUpdateResponse updateLocation(Integer locationId, LocationUpdateRequest location);

    public Optional<Location> getLocationById(Integer locationId);

    public List<Location> getAllLocations();

    public void deleteLocation(Integer locationId);

    public Location addParentLocation(int parentId, Location location);


}

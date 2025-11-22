package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.LocationMapper;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.service.LocationService;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
  private final LocationRepository locationRepository;
  private final LocationMapper locationMapper;

  @Override
  public LocationResponse addLocation(LocationAddingRequest request) {
    if (request.getLocationName() == null || request.getLocationName().trim().isEmpty()) {
      throw new ConflictException("LocationName is required");
    }

    // NOTE: TYPE SHOULD BE IN ENGLISH (City / District / Ward, ... )
    if (request.getLocationType() == null || request.getLocationType().trim().isEmpty()) {
      throw new ConflictException("LocationType is required");
    }

    if (locationRepository.findByLocationName(request.getLocationName()).isPresent()) {
      throw new ConflictException("Location already exists");
    }

    Location newLocation = locationMapper.toLocation(request);
    newLocation.setActive(true);
    Location savedLocation = locationRepository.save(newLocation);
    return locationMapper.toLocationResponse(savedLocation);
  }

  @Override
  // Workflow: Client -> Controller (JSON) -> MapStruct maps JSON to object ->
  // Update necessary
  // fields, including parent -> Map back to responseDTO -> Client (JSON string)
  public LocationResponse updateLocation(Long locationId, LocationUpdateRequest request) {
    Location existtingLocation = locationRepository
        .findById(locationId)
        .orElseThrow(() -> new NotFoundException("Location does not exist"));

    // Get new update information from the request and map it to the current
    // location to be updated
    // via the mapper with locationId
    locationMapper.updateLocationFromRequest(request, existtingLocation);

    // Handle parent
    if (request.getParentLocationId() != null) {
      Location parentLocation = locationRepository
          .findById(request.getParentLocationId())
          .orElseThrow(() -> new NotFoundException("Parent location does not exist"));
      existtingLocation.setParent(parentLocation);
    }
    Location updatedLocation = locationRepository.save(existtingLocation);
    return locationMapper.toLocationResponse(updatedLocation);
  }

  @Override
  public LocationResponse getLocationById(Long locationId) {
    Location location = locationRepository
        .findById(locationId)
        .orElseThrow(() -> new NotFoundException("Location does not exist"));
    return locationMapper.toLocationResponse(location);
  }

  @Override
  public LocationResponse getLocationByName(String locationName) {
    if (locationName == null || locationName.trim().isEmpty()) {
      throw new ConflictException("LocationName is required");
    }
    Location location = locationRepository
        .findByLocationName(locationName)
        .orElseThrow(() -> new NotFoundException("Location does not exist"));
    return locationMapper.toLocationResponse(location);
  }

  @Override
  public List<LocationResponse> getAllLocations() {
    List<Location> locations = locationRepository.findAll();
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location location : locations) {
      locationResponses.add(locationMapper.toLocationResponse(location));
    }
    return locationResponses;
  }

  @Override
  public List<LocationResponse> getAllLocationsIsActiveTrue() {
    List<Location> locations = locationRepository.findByIsActiveIsTrue();
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location location : locations) {
      locationResponses.add(locationMapper.toLocationResponse(location));
    }
    return locationResponses;
  }

  @Override
  public LocationResponse deleteLocation(Long locationId) {
    Location location = locationRepository
        .findById(locationId)
        .orElseThrow(() -> new NotFoundException("Location does not exist"));

    List<Location> children = getChildLocationEntity(location);

    if (!children.isEmpty()) {
      // If there are children, deactivate the parent location
      location.setActive(false);
      locationRepository.save(location);
    } else {
      // If no children, delete the location
      locationRepository.delete(location);
    }
    return locationMapper.toLocationResponse(location);
  }

  @Override
  public List<LocationResponse> getChildLocation(Location location) {
    List<Location> children = locationRepository.findByParent(location);
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location child : children) {
      locationResponses.add(locationMapper.toLocationResponse(child));
    }
    return locationResponses;
  }

  private List<Location> getChildLocationEntity(Location location) {
    return locationRepository.findByParent(location);
  }

  // ====== SERVING SEARCH FUNCTIONALITY FOR USERS
  // ==================================
  // ====== NOTE: TYPE SHOULD BE IN ENGLISH (City / District / Ward, ... )
  @Override
  public List<LocationResponse> getCities() {
    List<Location> cities = locationRepository.findByLocationTypeAndIsActiveTrueOrderByLocationNameAsc("City");
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location city : cities) {
      locationResponses.add(locationMapper.toLocationResponse(city));
    }
    return locationResponses;
  }

  @Override
  public List<LocationResponse> getDistrictsByCityId(Long cityId) {
    if (cityId == null) {
      throw new IllegalArgumentException("CityId is required for filtering");
    }
    Location city = locationRepository
        .findById(cityId)
        .orElseThrow(() -> new IllegalArgumentException("City not found"));
    List<Location> districts = locationRepository.findByParentAndLocationType(city, "District");
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location district : districts) {
      locationResponses.add(locationMapper.toLocationResponse(district));
    }
    return locationResponses;
  }

  @Override
  public List<LocationResponse> getWardByDistrictId(Long districtId) {
    if (districtId == null) {
      throw new IllegalArgumentException("DistrictId is required for filtering");
    }
    Location district = locationRepository
        .findById(districtId)
        .orElseThrow(() -> new IllegalArgumentException("District not found"));
    List<Location> wards = locationRepository.findByParentAndLocationType(district, "Ward");
    List<LocationResponse> locationResponses = new ArrayList<>();
    for (Location ward : wards) {
      locationResponses.add(locationMapper.toLocationResponse(ward));
    }
    return locationResponses;
  }

  @Override
  public Location findAndParseLocationFromAddress(String address) {

    // B1: check valid
    if (address == null || address.isEmpty()) {
      throw new NotFoundException("Address cannot be null or empty");
    }

    // B2: tách chuỗi theo dấu phẫy
    String[] addressParts = address.split(",");

    String locationName = null;
    String locationType = null;
    int priority = Integer.MAX_VALUE;

    for (String part : addressParts) {

      // loại bỏ khoảng trắng thừa từng chuỗi con
      String trimmed = part.trim();

      // =================== kiểm tra Thành phố ====================
      // nếu là thành phố thủ đức
      if (trimmed.equalsIgnoreCase("Thành phố Thủ Đức")) {
        locationName = "Hồ Chí Minh";
        locationType = "City";
        priority = 0;
        continue; // bỏ qua các if tiếp theo trong vòng lặp => mặc dịnh return luôn là HCM
      }

      if ((trimmed.startsWith("Thành phố")
          || trimmed.startsWith("TP ")
          || trimmed.startsWith("TP.")) && priority > 1) {
        locationName = trimmed
            .replaceFirst("(Thành phố|TP\\.?) ", "");
        locationType = "City";
        priority = 1;

        // =================== kiểm tra Tỉnh ====================
      } else if (trimmed.startsWith("Tỉnh ") && priority > 2) {
        locationName = trimmed.replace("Tỉnh ", "");
        locationType = "Province";
        priority = 2;

        // =================== kiểm tra Quận/Huyện ====================
      } else if ((trimmed.startsWith("Quận ") ||
          trimmed.startsWith("Huyện ")) && priority > 3) {
        locationName = trimmed.replaceFirst("(Quận|Huyện) ", "");
        locationType = "District";
        priority = 3;

        // =================== kiểm tra Phường/Xã ====================
      } else if ((trimmed.startsWith("Phường ") ||
          trimmed.startsWith("Xã ")) && priority > 4) {
        locationName = trimmed.replaceFirst("(Phường|Xã) ", "");
        locationType = "Ward";
        priority = 4;
      }
    }

    if (locationName == null || locationType == null) {
      throw new NotFoundException(
          "Could not parse City or Province from address: " + address);
    }

    // gọi hàm tìm hoặc tạo mới Location
    return findOrCreateLocation(locationName, locationType);
  }

  private Location findOrCreateLocation(String locationName, String locationType) {
    List<Location> existingLocations = locationRepository
        .findByLocationTypeAndIsActiveTrueOrderByLocationNameAsc(locationType);

    for (Location location : existingLocations) {
      if (location.getLocationName().equalsIgnoreCase(locationName)) {
        return location; // đã tìm thấy trả về luôn
        // => FE có thể lấy và gọi locaion.Id trong trang station (Admin)
      }
    }

    // tạo mơi nếu không tìm thấy
    Location newLocation = new Location();
    newLocation.setLocationName(locationName);
    newLocation.setLocationType(locationType);
    newLocation.setActive(true);
    newLocation.setParent(null);
    newLocation.setLatitude(null);
    newLocation.setLongitude(null);
    newLocation.setRadius(null); // Các vùng này là vùng rộng, không cần tọa độ chính xác

    return locationRepository.save(newLocation);
  }
}

package spring_boot.project_swp.service;

import java.util.List;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.StationResponse;

@Service
public interface StationService {
  public List<StationResponse> getAllStations();

  public List<StationResponse> getAllStationsByLocationId(Long locationId);

  public StationResponse findStationById(Long id);

  public StationResponse findStationByName(String name);

  public StationResponse addStation(StationAddingRequest request);

  public StationResponse updateStation(Long stationId, StationUpdateRequest request);

  public void deleteStationById(Long id);

  public List<StationResponse> findStationsByCityId(Long cityId);

  public List<StationResponse> findStationsByDistrictId(Long cityId, Long districtId);
}

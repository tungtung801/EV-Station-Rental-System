package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.respone.StationResponse;
import spring_boot.project_swp.entity.Station;

import java.util.List;

@Service
public interface StationService {
    public List<StationResponse> getAllStations();

    public StationResponse findStationById(Integer id);

    public Station findStationByName(String name);

    public StationResponse addStation (StationAddingRequest request);

    public StationResponse updateStation(Integer stationId, StationUpdateRequest request);

    public StationResponse deleteStationById(Integer id);
}

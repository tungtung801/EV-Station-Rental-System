package spring_boot.project_swp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.respone.StationResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.mapper.LocationMapper;
import spring_boot.project_swp.mapper.StationMapper;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.service.StationService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {
    private final StationRepository stationRepository;
    private final StationMapper stationMapper;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public List<StationResponse> getAllStations() {
        return stationMapper.toStationResponseList(stationRepository.findAll());
    }

    @Override
    public StationResponse findStationById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("StationID is required");
        }
        return stationMapper.toStationResponse(stationRepository.findStationByStationId(id));
    }

    @Override
    public Station findStationByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("StationName is required");
        }
        return stationRepository.findStationByStationName(name);
    }

    @Override
    public StationResponse addStation(StationAddingRequest request) {
        if (request.getStationName() == null || request.getStationName().trim().isEmpty()) {
            throw new IllegalArgumentException("StationName is required");
        }

        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }

        if (stationRepository.findStationByStationName(request.getStationName()) != null) {
            throw new IllegalArgumentException("Station already exists");
        }

        Station newStation = stationMapper.toStation(request);

        newStation.setActive(true);
        newStation.setAvailableDocks(request.getTotalDocks());

        Location location = locationRepository.findByLocationId(request.getLocationId());
        if (location == null) {
            throw new IllegalArgumentException("Location not found with id " + request.getLocationId());
        }
        newStation.setLocation(location);

        stationRepository.save(newStation);

        return stationMapper.toStationResponse(newStation);
    }

    @Override
    public StationResponse updateStation(Integer stationId, StationUpdateRequest request) {
        Station station = stationRepository.findStationByStationId(stationId);

        if (station == null) {
            throw new IllegalArgumentException("Station does not exist");
        }
        stationMapper.updateStationFromRequest(request, station);

        if (request.getLocationId() != null) {
            Location newLocation = locationRepository.findByLocationId(request.getLocationId());
            if (newLocation != null) {
                station.setLocation(newLocation);
            } else {
                throw new IllegalArgumentException("Location does not exist with id: " + request.getLocationId());
            }
        }
        Station updatedStation = stationRepository.save(station);

        return stationMapper.toStationResponse(updatedStation);
    }

    @Override
    public StationResponse deleteStationById(Integer id) {
        Station station = stationRepository.findStationByStationId(id);
        if (station != null) {
            station.setActive(false);
        }
        return stationMapper.toStationResponse(stationRepository.save(station));
    }
}

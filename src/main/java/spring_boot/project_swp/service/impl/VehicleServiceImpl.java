package spring_boot.project_swp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.respone.VehicleResponse;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.mapper.VehicleMapper;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.VehicleService;

import java.util.List;

@Service
@AllArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final StationRepository stationRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponse addVehicle(VehicleRequest request) {

        Vehicle vehicle = vehicleMapper.toVehicle(request);
        VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("Vehicle model not found"));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        vehicle.setVehicleModel(model);
        vehicle.setStation(station);
        vehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toVehicleRespone(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(int vehicleId, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicleMapper.updateVehicleFromRequest(request, vehicle);

        VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("Vehicle model not found"));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        vehicle.setVehicleModel(model);
        vehicle.setStation(station);

        vehicleRepository.save(vehicle);

        return vehicleMapper.toVehicleRespone(vehicle);
    }

    @Override
    public void deleteVehicle(int vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }

    @Override
    public List<VehicleResponse> findAll() {
    return vehicleMapper.toVehicleResponeList(vehicleRepository.findAll());
    }

    @Override
    public VehicleResponse findById(int vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return vehicleMapper.toVehicleRespone(vehicle);
    }
}

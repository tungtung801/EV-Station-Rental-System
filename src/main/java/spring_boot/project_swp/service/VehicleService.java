package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.respone.VehicleResponse;

import java.util.List;

public interface VehicleService {
    public VehicleResponse addVehicle(VehicleRequest request);
    public VehicleResponse updateVehicle(int vehicleId, VehicleRequest request);
    public void deleteVehicle(int vehicleId);
    public List<VehicleResponse> findAll();
    public VehicleResponse findById(int vehicleId);

}

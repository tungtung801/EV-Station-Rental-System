package spring_boot.project_swp.service;

import spring_boot.project_swp.entity.VehicleModel;

import java.util.List;

public interface VehicleModelService {
    public List<VehicleModel> getAllVehicleModels();
    public VehicleModel getVehicleModelById(int id);
    public boolean addVehicleModel(VehicleModel vehicleModel);
}

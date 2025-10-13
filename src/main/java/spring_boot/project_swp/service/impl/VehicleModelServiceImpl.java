package spring_boot.project_swp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.service.VehicleModelService;

import java.util.List;

@Service
@AllArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService{
private final VehicleModelRepository vehicleModelRepository;
    @Override
    public List<VehicleModel> getAllVehicleModels() {
        return vehicleModelRepository.findAll();
    }

    @Override
    public VehicleModel getVehicleModelById(int id) {
        return vehicleModelRepository.findById(id).orElse(null);
    }

    @Override
    public boolean addVehicleModel(VehicleModel vehicleModel) {
        return vehicleModelRepository.save(vehicleModel) != null;
    }

}

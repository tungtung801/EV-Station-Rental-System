package spring_boot.project_swp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.VehicleModelMapper;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.service.VehicleModelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService {
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleModelMapper vehicleModelMapper;

    @Override
    public List<VehicleModelResponse> getAllVehicleModels() {
        List<VehicleModel> vehicleModels = vehicleModelRepository.findAll();
        List<VehicleModelResponse> responses = new ArrayList<>();
        for (VehicleModel vehicleModel : vehicleModels) {
            responses.add(vehicleModelMapper.toVehicleModelResponse(vehicleModel));
        }
        return responses;
    }

    @Override
    public VehicleModelResponse getVehicleModelById(int id) {
        Optional<VehicleModel> vehicleModel = vehicleModelRepository.findByModelId(id);
        if (vehicleModel.isEmpty()) {
            throw new NotFoundException("Vehicle model not found with ID: " + id);
        }
        return vehicleModelMapper.toVehicleModelResponse(vehicleModel.get());
    }

    @Override
    public VehicleModelResponse addVehicleModel(VehicleModelRequest request) {
        Optional<VehicleModel> existingVehicleModel = vehicleModelRepository.findByModelName(request.getModelName());
        if (existingVehicleModel.isPresent()) {
            throw new ConflictException("Vehicle model with name '" + request.getModelName() + "' already exists.");
        }
        VehicleModel vehicleModel = vehicleModelMapper.toVehicleModel(request);
        VehicleModel savedVehicleModel = vehicleModelRepository.save(vehicleModel);
        return vehicleModelMapper.toVehicleModelResponse(savedVehicleModel);
    }

    @Override
    public VehicleModelResponse updateVehicleModel(int id, VehicleModelRequest request) {
        Optional<VehicleModel> existingVehicleModelOptional = vehicleModelRepository.findByModelId(id);
        if (existingVehicleModelOptional.isEmpty()) {
            throw new NotFoundException("Vehicle model not found with ID: " + id);
        }

        VehicleModel existingVehicleModel = existingVehicleModelOptional.get();

        Optional<VehicleModel> vehicleModelWithSameName = vehicleModelRepository.findByModelName(request.getModelName());
        if (vehicleModelWithSameName.isPresent() && vehicleModelWithSameName.get().getModelId() != id) {
            throw new ConflictException("Vehicle model with name '" + request.getModelName() + "' already exists.");
        }

        vehicleModelMapper.updateVehicleModelFromRequest(request, existingVehicleModel);
        VehicleModel updatedVehicleModel = vehicleModelRepository.save(existingVehicleModel);
        return vehicleModelMapper.toVehicleModelResponse(updatedVehicleModel);
    }

    @Override
    public void deleteVehicleModel(int id) {
        Optional<VehicleModel> vehicleModel = vehicleModelRepository.findByModelId(id);
        if (vehicleModel.isEmpty()) {
            throw new NotFoundException("Vehicle model not found with ID: " + id);
        }
        vehicleModelRepository.delete(vehicleModel.get());
    }
}

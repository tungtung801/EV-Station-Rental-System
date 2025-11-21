package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.VehicleModelMapper;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.service.VehicleModelService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleModelServiceImpl implements VehicleModelService {

  final VehicleModelRepository vehicleModelRepository;
  final VehicleModelMapper vehicleModelMapper;

  @Override
  public List<VehicleModelResponse> getAllVehicleModels() {
    List<VehicleModel> models = vehicleModelRepository.findAll();
    List<VehicleModelResponse> responses = new ArrayList<>();
    for (VehicleModel model : models) {
      responses.add(vehicleModelMapper.toVehicleModelResponse(model));
    }
    return responses;
  }

  @Override
  public VehicleModelResponse getVehicleModelById(Long id) {
    VehicleModel model =
        vehicleModelRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + id));
    return vehicleModelMapper.toVehicleModelResponse(model);
  }

  @Override
  @Transactional
  public VehicleModelResponse addVehicleModel(VehicleModelRequest request) {
    if (vehicleModelRepository.existsByModelName(request.getModelName())) {
      throw new ConflictException("Model name '" + request.getModelName() + "' already exists.");
    }
    VehicleModel model = vehicleModelMapper.toVehicleModel(request);
    return vehicleModelMapper.toVehicleModelResponse(vehicleModelRepository.save(model));
  }

  @Override
  @Transactional
  public VehicleModelResponse updateVehicleModel(Long id, VehicleModelRequest request) {
    VehicleModel existingModel =
        vehicleModelRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + id));

    // Check trùng tên (trừ chính nó)
    vehicleModelRepository
        .findByModelName(request.getModelName())
        .ifPresent(
            duplicate -> {
              if (!duplicate.getModelId().equals(id)) {
                throw new ConflictException(
                    "Model name '" + request.getModelName() + "' already exists.");
              }
            });

    vehicleModelMapper.updateVehicleModelFromRequest(request, existingModel);
    return vehicleModelMapper.toVehicleModelResponse(vehicleModelRepository.save(existingModel));
  }

  @Override
  @Transactional
  public void deleteVehicleModel(Long id) {
    if (!vehicleModelRepository.existsById(id)) {
      throw new NotFoundException("Model not found with ID: " + id);
    }
    vehicleModelRepository.deleteById(id);
  }
}

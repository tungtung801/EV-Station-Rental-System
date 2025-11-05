package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.VehicleModel;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleModelMapper {
  @Mapping(target = "modelId", ignore = true)
  VehicleModel toVehicleModel(VehicleModelRequest request);

  @Mapping(target = "modelId", ignore = true)
  void updateVehicleModelFromRequest(
      VehicleModelRequest request, @MappingTarget VehicleModel vehicleModel);

  VehicleModelResponse toVehicleModelResponse(VehicleModel vehicleModel);

  List<VehicleModelResponse> toVehicleModelResponseList(List<VehicleModel> vehicleModels);

  VehicleResponse.ModelInfo toModelInfo(VehicleModel vehicleModel);
}

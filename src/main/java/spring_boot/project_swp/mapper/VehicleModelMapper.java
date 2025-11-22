package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.*;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;
import spring_boot.project_swp.entity.VehicleModel;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleModelMapper {

    @Mapping(target = "modelId", ignore = true)
    @Mapping(target = "vehicles", ignore = true) // Ignore list xe con
    VehicleModel toVehicleModel(VehicleModelRequest request);

    // Thêm dòng này vào
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "modelId", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    void updateVehicleModelFromRequest(
            VehicleModelRequest request, @MappingTarget VehicleModel vehicleModel);

    VehicleModelResponse toVehicleModelResponse(VehicleModel vehicleModel);

    List<VehicleModelResponse> toVehicleModelResponseList(List<VehicleModel> vehicleModels);
}
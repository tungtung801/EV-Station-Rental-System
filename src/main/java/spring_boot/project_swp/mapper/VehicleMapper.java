package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Vehicle;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class, VehicleModelMapper.class, StationMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {
  @Mapping(target = "vehicleId", ignore = true)
  @Mapping(target = "vehicleModel", ignore = true)
  @Mapping(target = "station", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  Vehicle toVehicle(VehicleRequest request);

  @Mapping(target = "vehicleId", ignore = true)
  @Mapping(target = "vehicleModel", ignore = true)
  @Mapping(target = "station", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  void updateVehicleFromRequest(VehicleUpdateRequest request, @MappingTarget Vehicle vehicle);

  @Mapping(source = "station", target = "currentStation")
  @Mapping(source = "vehicleModel", target = "model")
  VehicleResponse toVehicleResponse(Vehicle vehicle);

  List<VehicleResponse> toVehicleResponseList(List<Vehicle> vehicles);
}

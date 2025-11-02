package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;
import spring_boot.project_swp.entity.VehicleChecks;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleCheckMapper {

  @Mapping(target = "checkId", ignore = true)
  @Mapping(target = "checkDate", ignore = true)
  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "staff", source = "staffId")
  VehicleChecks toVehicleChecks(VehicleCheckRequest request);

  @Mapping(target = "rentalId", source = "rental.rentalId")
  @Mapping(target = "staffId", source = "staff.userId")
  VehicleCheckResponse toVehicleCheckResponse(VehicleChecks vehicleChecks);

  @Mapping(target = "checkId", ignore = true)
  @Mapping(target = "checkDate", ignore = true)
  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "staff", source = "staffId")
  void updateVehicleChecks(@MappingTarget VehicleChecks vehicleChecks, VehicleCheckRequest request);
}

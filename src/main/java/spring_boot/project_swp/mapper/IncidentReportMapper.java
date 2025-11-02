package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.entity.IncidentReports;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IncidentReportMapper {

  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "vehicleCheck", source = "checkId")
  IncidentReports toIncidentReports(IncidentReportRequest request);

  @Mapping(target = "rentalId", source = "rental.rentalId")
  @Mapping(target = "vehicleId", source = "vehicle.vehicleId")
  @Mapping(target = "userId", source = "user.userId")
  @Mapping(target = "checkId", source = "vehicleCheck.checkId")
  IncidentReportResponse toIncidentReportResponse(IncidentReports incidentReports);

  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "vehicleCheck", source = "checkId")
  void updateIncidentReports(
      IncidentReportRequest request, @MappingTarget IncidentReports incidentReports);
}

package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleModel;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

  // --- REQUEST TO ENTITY ---
  // Model và Station sẽ được set trong Service (tìm theo ID) -> Ignore tại đây
  @Mapping(target = "vehicleId", ignore = true)
  @Mapping(target = "vehicleModel", ignore = true)
  @Mapping(target = "station", ignore = true)
  @Mapping(target = "imageUrl", ignore = true) // Ảnh upload riêng
  @Mapping(target = "rentals", ignore = true)
  @Mapping(target = "incidentReports", ignore = true)
  Vehicle toVehicle(VehicleRequest request);

  // --- UPDATE REQUEST ---
  @Mapping(target = "vehicleId", ignore = true)
  @Mapping(target = "vehicleModel", ignore = true)
  @Mapping(target = "station", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  void updateVehicleFromRequest(VehicleUpdateRequest request, @MappingTarget Vehicle vehicle);

  // --- ENTITY TO RESPONSE ---
  @Mapping(source = "station", target = "currentStation") // Map Station entity -> StationInfo
  @Mapping(source = "vehicleModel", target = "model") // Map VehicleModel entity -> ModelInfo
  VehicleResponse toVehicleResponse(Vehicle vehicle);

  List<VehicleResponse> toVehicleResponseList(List<Vehicle> vehicles);

  // --- HELPER MAPPINGS (Để map các object con bên trong Response) ---

  // Tự động map Station Entity -> StationInfo DTO
  VehicleResponse.StationInfo toStationInfo(Station station);

  // Tự động map VehicleModel Entity -> ModelInfo DTO
  VehicleResponse.ModelInfo toModelInfo(VehicleModel vehicleModel);
}

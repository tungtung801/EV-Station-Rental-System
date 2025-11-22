package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.*;
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
    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "station", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "rentals", ignore = true)
    @Mapping(target = "incidentReports", ignore = true)
    Vehicle toVehicle(VehicleRequest request);

    // --- UPDATE REQUEST (SỬA LẠI CHỖ NÀY) ---
    // Thêm dòng này để nếu field gửi lên là NULL thì KHÔNG update vào Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "station", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "rentals", ignore = true)
    @Mapping(target = "incidentReports", ignore = true)
    void updateVehicleFromRequest(VehicleUpdateRequest request, @MappingTarget Vehicle vehicle);

    // --- ENTITY TO RESPONSE ---
    @Mapping(source = "station", target = "currentStation")
    @Mapping(source = "vehicleModel", target = "model")
    VehicleResponse toVehicleResponse(Vehicle vehicle);

    List<VehicleResponse> toVehicleResponseList(List<Vehicle> vehicles);

    // --- HELPER MAPPINGS ---
    VehicleResponse.StationInfo toStationInfo(Station station);

    // Chú ý: MapStruct sẽ tự map capacityKWh nếu tên trùng nhau
    VehicleResponse.ModelInfo toModelInfo(VehicleModel vehicleModel);
}
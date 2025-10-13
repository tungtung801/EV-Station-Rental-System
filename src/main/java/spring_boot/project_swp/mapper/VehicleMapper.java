package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.respone.VehicleResponse;
import spring_boot.project_swp.entity.Vehicle;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StationMapper.class, VehicleModelMapper.class})
public interface VehicleMapper {
    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "station", ignore = true)
    Vehicle toVehicle(VehicleRequest request);


    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "station", ignore = true)
    void updateVehicleFromRequest(VehicleRequest request, @MappingTarget Vehicle vehicle);

    @Mapping(source = "station", target = "currentStation")
    @Mapping(source = "vehicleModel", target = "model")
    VehicleResponse toVehicleRespone(Vehicle vehicle);

    List<VehicleResponse> toVehicleResponeList(List<Vehicle> vehicles);

}

package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import spring_boot.project_swp.dto.respone.VehicleResponse;
import spring_boot.project_swp.entity.VehicleModel;

@Mapper(componentModel = "spring")
public interface VehicleModelMapper {
    VehicleResponse.ModelInfo toModelInfo(VehicleModel vehicleModel);
}

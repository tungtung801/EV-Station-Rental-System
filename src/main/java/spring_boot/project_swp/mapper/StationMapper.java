package spring_boot.project_swp.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.StationResponse;
import spring_boot.project_swp.dto.respone.VehicleResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Station;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStation(StationAddingRequest request);

    StationResponse toStationResponse(Station station);

    List<StationResponse> toStationResponseList(List<Station> stations);

    // Map từ StationUpdateRequest sang Station entity (cho update)
    @Mapping(target = "stationId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateStationFromRequest(StationUpdateRequest request, @MappingTarget Station station);

    @AfterMapping // tự động gọi đến mapper này sau khi chuyeenr doi thành ResponseDto
    default void mapLocationDetails(@MappingTarget StationResponse response, Station station){
        Location currentlocation = station.getLocation();

        while(currentlocation != null){
            String locationName = currentlocation.getLocationName();
            String locationType = currentlocation.getLocationType();

            if ("Ward".equalsIgnoreCase(locationType)){
                response.setWard(locationName);
            }else if ("District".equalsIgnoreCase(locationType)){
                response.setDistrict(locationName);
            }else if ("City".equalsIgnoreCase(locationType)){
                response.setCity(locationName);
            }

            currentlocation = currentlocation.getParent();
        }
    }
    VehicleResponse.StationInfo toStationInfo(Station station);
}

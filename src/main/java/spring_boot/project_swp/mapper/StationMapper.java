package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.StationAddingRequest;
import spring_boot.project_swp.dto.request.StationUpdateRequest;
import spring_boot.project_swp.dto.response.StationResponse;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.StationStatusEnum;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MapperUtils.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StationMapper {

    @Mapping(target = "isActive", source = "isActive")
    Station toStation(StationAddingRequest request);

    StationResponse toStationResponse(Station station);

    List<StationResponse> toStationResponseList(List<Station> stations);

    // Map từ StationUpdateRequest sang Station entity (cho update)
    @Mapping(target = "stationId", ignore = true)
    @Mapping(target = "location", source = "locationId")
    void updateStationFromRequest(StationUpdateRequest request, @MappingTarget Station station);

    // ✅ AfterMapping để set default ACTIVE nếu isActive null
    @AfterMapping
    default void ensureActiveDefault(@MappingTarget Station station, StationAddingRequest request) {
        if (station.getIsActive() == null) {
            station.setIsActive(StationStatusEnum.ACTIVE);
        }
    }

    // ✅ AfterMapping cho update - set ACTIVE nếu null
    @AfterMapping
    default void ensureActiveDefaultOnUpdate(@MappingTarget Station station, StationUpdateRequest request) {
        if (station.getIsActive() == null) {
            station.setIsActive(StationStatusEnum.ACTIVE);
        }
    }

    @AfterMapping // tự động gọi đến mapper này sau khi chuyeenr doi thành ResponseDto
    default void mapLocationDetails(@MappingTarget StationResponse response, Station station) {
        Location currentlocation = station.getLocation();

        while (currentlocation != null) {
            String locationName = currentlocation.getLocationName();
            String locationType = currentlocation.getLocationType();

            if ("Ward".equalsIgnoreCase(locationType)) {
                response.setWard(locationName);
            } else if ("District".equalsIgnoreCase(locationType)) {
                response.setDistrict(locationName);
            } else if ("City".equalsIgnoreCase(locationType)) {
                response.setCity(locationName);
            }

            currentlocation = currentlocation.getParent();
        }
    }

    VehicleResponse.StationInfo toStationInfo(Station station);
}


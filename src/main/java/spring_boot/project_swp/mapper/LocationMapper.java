package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.LocationAddingRequest;
import spring_boot.project_swp.dto.request.LocationUpdateRequest;
import spring_boot.project_swp.dto.response.LocationResponse;
import spring_boot.project_swp.entity.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    // Dùng de add new 1 Location tu request
    Location toLocation(LocationAddingRequest request);

    // Dung cho moi truong hop tra ve get / put /
    @Mapping(source = "parent.locationId", target = "parentLocationId")
    LocationResponse toLocationResponse(Location location);

    // For updating existing location
    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateLocationFromRequest(LocationUpdateRequest request, @MappingTarget Location location);
}

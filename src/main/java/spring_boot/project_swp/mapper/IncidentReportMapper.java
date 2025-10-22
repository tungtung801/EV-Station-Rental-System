package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.entity.IncidentReports;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleChecks;

@Mapper(componentModel = "spring")
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
    void updateIncidentReports(IncidentReportRequest request, @MappingTarget IncidentReports incidentReports);

    default Rental mapRentalIdToRental(Long rentalId) {
        if (rentalId == null) {
            return null;
        }
        return new Rental(rentalId);
    }

    default Vehicle mapVehicleIdToVehicle(Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return new Vehicle(vehicleId);
    }

    default User mapUserIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return new User(userId);
    }

    default VehicleChecks mapCheckIdToVehicleChecks(Long checkId) {
        if (checkId == null) {
            return null;
        }
        return new VehicleChecks(checkId);
    }
}
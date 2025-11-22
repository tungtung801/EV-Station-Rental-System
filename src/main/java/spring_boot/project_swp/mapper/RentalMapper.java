package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Rental;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

    // --- ENTITY TO RESPONSE (GIỮ NGUYÊN) ---
    @Mapping(source = "booking.bookingId", target = "bookingId")
    @Mapping(source = "renter.userId", target = "userId")
    @Mapping(source = "renter.fullName", target = "renterName")
    @Mapping(source = "vehicle.vehicleId", target = "vehicleId")
    @Mapping(source = "vehicle.vehicleModel.modelName", target = "vehicleModel")
    @Mapping(source = "pickupStation.stationId", target = "pickupStationId")
    @Mapping(source = "pickupStation.stationName", target = "pickupStationName")
    @Mapping(source = "returnStation.stationId", target = "returnStationId")
    @Mapping(source = "returnStation.stationName", target = "returnStationName")
    @Mapping(source = "pickupStaff.userId", target = "pickupStaffId")
    @Mapping(source = "pickupStaff.fullName", target = "pickupStaffName")
    @Mapping(source = "returnStaff.userId", target = "returnStaffId")
    @Mapping(source = "returnStaff.fullName", target = "returnStaffName")
    RentalResponse toRentalResponse(Rental rental);

    // Thêm phương thức để Map List
    List<RentalResponse> toRentalResponseList(List<Rental> rentals);

}
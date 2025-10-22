package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Rental;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, VehicleMapper.class, StationMapper.class, BookingMapper.class})
public interface RentalMapper {

    @Mapping(target = "rentalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(source = "bookingId", target = "booking.bookingId")
    @Mapping(source = "renterId", target = "renter.userId")
    @Mapping(source = "vehicleId", target = "vehicle.vehicleId")
    @Mapping(source = "pickupStationId", target = "pickupStation.stationId")
    @Mapping(source = "returnStationId", target = "returnStation.stationId")
    @Mapping(source = "pickupStaffId", target = "pickupStaff.userId")
    @Mapping(source = "returnStaffId", target = "returnStaff.userId")
    Rental toRental(RentalRequest request);

    @Mapping(source = "booking.bookingId", target = "bookingId")
    @Mapping(source = "renter.userId", target = "renterId")
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

    @Mapping(target = "rentalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(source = "bookingId", target = "booking.bookingId")
    @Mapping(source = "renterId", target = "renter.userId")
    @Mapping(source = "vehicleId", target = "vehicle.vehicleId")
    @Mapping(source = "pickupStaffId", target = "pickupStaff.userId")
    @Mapping(source = "returnStaffId", target = "returnStaff.userId")
    @Mapping(target = "pickupStation", ignore = true)
    @Mapping(target = "returnStation", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "renter", ignore = true)
    void updateRentalFromRequest(RentalRequest request, @MappingTarget Rental rental);
}
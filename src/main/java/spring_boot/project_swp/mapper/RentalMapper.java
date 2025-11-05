package spring_boot.project_swp.mapper;

import org.mapstruct.*;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Rental;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

  @Mapping(target = "rentalId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "startActual", ignore = true)
  @Mapping(target = "endActual", ignore = true)
  @Mapping(target = "total", ignore = true)
  @Mapping(target = "booking", source = "bookingId")
  @Mapping(target = "renter", source = "userId")
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "pickupStation", source = "pickupStationId")
  @Mapping(target = "returnStation", source = "returnStationId")
  @Mapping(target = "pickupStaff", source = "pickupStaffId")
  @Mapping(target = "returnStaff", source = "returnStaffId")
  Rental toRental(RentalRequest request);

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

  @Mapping(target = "rentalId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "startActual", ignore = true)
  @Mapping(target = "endActual", ignore = true)
  @Mapping(target = "total", ignore = true)
  @Mapping(target = "booking", source = "bookingId")
  @Mapping(target = "renter", source = "userId")
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "pickupStaff", source = "pickupStaffId")
  @Mapping(target = "returnStaff", source = "returnStaffId")
  @Mapping(target = "pickupStation", ignore = true)
  @Mapping(target = "returnStation", ignore = true)
  void updateRentalFromRequest(RentalRequest request, @MappingTarget Rental rental);
}

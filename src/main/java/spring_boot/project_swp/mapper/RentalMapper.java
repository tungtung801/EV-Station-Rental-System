package spring_boot.project_swp.mapper;

import org.mapstruct.*;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Rental;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    // uses = {MapperUtils.class}, // Bỏ nếu ko dùng
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

  // --- REQUEST TO ENTITY ---
  // Các trường quan hệ (Booking, User, Vehicle...) sẽ được set trong Service
  @Mapping(target = "rentalId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "booking", ignore = true)
  @Mapping(target = "renter", ignore = true)
  @Mapping(target = "vehicle", ignore = true)
  @Mapping(target = "pickupStation", ignore = true)
  @Mapping(target = "returnStation", ignore = true)
  @Mapping(target = "pickupStaff", ignore = true)
  @Mapping(target = "returnStaff", ignore = true)
  @Mapping(target = "payments", ignore = true)
  @Mapping(target = "rentalDiscounts", ignore = true)
  Rental toRental(RentalRequest request);

  // --- ENTITY TO RESPONSE ---
  // MapStruct tự lấy được các trường con (nested properties)
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

  // --- UPDATE ---
  @Mapping(target = "rentalId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "booking", ignore = true)
  @Mapping(target = "renter", ignore = true)
  @Mapping(target = "vehicle", ignore = true)
  @Mapping(target = "pickupStation", ignore = true)
  @Mapping(target = "returnStation", ignore = true)
  @Mapping(target = "pickupStaff", ignore = true)
  @Mapping(target = "returnStaff", ignore = true)
  void updateRentalFromRequest(RentalRequest request, @MappingTarget Rental rental);
}

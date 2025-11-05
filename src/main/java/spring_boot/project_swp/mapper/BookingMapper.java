package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.Booking;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class, VehicleMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

  @Mapping(target = "bookingId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "depositPercent", ignore = true)
  @Mapping(target = "expectedTotal", ignore = true)
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "user", source = "userId")
  Booking toBooking(BookingRequest request);

  @Mapping(source = "user.userId", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(target = "vehicle", source = "vehicle")
  BookingResponse toBookingResponse(Booking booking);

  @Mapping(source = "userId", target = "user")
  @Mapping(target = "vehicle", source = "vehicle")
  @Mapping(target = "bookingId", source = "bookingId")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "depositPercent", source = "depositPercent")
  @Mapping(target = "expectedTotal", source = "expectedTotal")
  Booking toBooking(BookingResponse response);

  @Mapping(target = "bookingId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "depositPercent", ignore = true)
  @Mapping(target = "expectedTotal", ignore = true)
  @Mapping(target = "user", source = "userId")
  void updateBookingFromRequest(BookingRequest request, @MappingTarget Booking booking);
}

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
  @Mapping(target = "vehicle", source = "vehicleId")
  @Mapping(target = "bookingType", source = "bookingType") // Explicitly map bookingType
  Booking toBooking(BookingRequest request);

  // Map user fields (id + full name) into BookingResponse so frontend can display customer name
  @Mapping(target = "userId", source = "user.userId")
  @Mapping(target = "userName", source = "user.fullName")
  @Mapping(target = "vehicle", source = "vehicle")
  BookingResponse toBookingResponse(Booking booking);

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
  @Mapping(target = "bookingType", source = "bookingType") // Explicitly map bookingType
  void updateBookingFromRequest(BookingRequest request, @MappingTarget Booking booking);
}

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
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "vehicle", source = "vehicleId")
  Booking toBooking(BookingRequest request);

  @Mapping(source = "user.userId", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(target = "vehicle", source = "vehicle")
  BookingResponse toBookingResponse(Booking booking);

  @Mapping(target = "bookingId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "vehicle", source = "vehicleId")
  void updateBookingFromRequest(BookingRequest request, @MappingTarget Booking booking);
}

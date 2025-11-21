package spring_boot.project_swp.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.Booking;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {VehicleMapper.class}, // Import để map VehicleResponse
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

  @Mapping(target = "bookingId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "totalAmount", ignore = true) // Tính toán trong Service
  @Mapping(target = "vehicle", ignore = true) // Set tay trong Service
  @Mapping(target = "user", ignore = true) // Set tay (User đang login)
  Booking toBooking(BookingRequest request);

  @Mapping(target = "userId", source = "user.userId")
  @Mapping(target = "userName", source = "user.fullName")
  @Mapping(target = "userPhone", source = "user.phoneNumber")
  @Mapping(target = "vehicle", source = "vehicle") // MapStruct dùng VehicleMapper để map cái này
  BookingResponse toBookingResponse(Booking booking);

  List<BookingResponse> toBookingResponseList(List<Booking> bookings);
}

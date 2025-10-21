package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.Booking;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {VehicleMapper.class})
public interface BookingMapper {

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "userId", target = "user.userId")
    @Mapping(source = "vehicleId", target = "vehicle.vehicleId")
    Booking toBooking(BookingRequest request);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "vehicle", target = "vehicle")
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "userId", target = "user.userId")
    @Mapping(source = "vehicleId", target = "vehicle.vehicleId")
    void updateBookingFromRequest(BookingRequest request, @MappingTarget Booking booking);
}
package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;

public interface BookingService {
  BookingResponse createBooking(BookingRequest request);

  BookingResponse getBookingById(Long bookingId);

  List<BookingResponse> getAllBookings();

  List<BookingResponse> getBookingsByUserId(Long userId);

  BookingResponse updateBooking(Long bookingId, BookingRequest request);

  void updateBookingStatus(Long bookingId, BookingStatusEnum status);

  List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId);
}

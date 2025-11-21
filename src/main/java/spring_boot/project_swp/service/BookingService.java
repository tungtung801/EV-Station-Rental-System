package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;

public interface BookingService {

  BookingResponse createBooking(String email, BookingRequest request);

  BookingResponse getBookingById(Long bookingId);

  List<BookingResponse> getAllBookings(String staffEmail, Long stationId);

  List<BookingResponse> getBookingsByUserId(Long userId);

  BookingResponse updateBookingStatus(
      Long bookingId, String email, BookingStatusUpdateRequest request);

  // Hàm lấy lịch bận của xe
  List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId);
}

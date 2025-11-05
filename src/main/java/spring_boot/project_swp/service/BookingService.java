package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.UserVerificationStatusResponse;

public interface BookingService {
  BookingResponse createBooking(String email, BookingRequest request);

  BookingResponse getBookingById(Long bookingId);

  List<BookingResponse> getAllBookings(String staffEmail, Long stationId);

  List<BookingResponse> getBookingsByUserId(Long userId);

  BookingResponse updateBooking(Long bookingId, String email, BookingRequest request);

  BookingResponse updateBookingStatus(
      Long bookingId, String email, BookingStatusUpdateRequest request);

  BookingResponse confirmDepositPayment(Long bookingId, String staffEmail);

  List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId);

  UserVerificationStatusResponse checkUserVerification(Long userId);
}

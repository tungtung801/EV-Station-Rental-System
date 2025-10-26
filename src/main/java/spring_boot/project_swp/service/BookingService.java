package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getBookingById(Integer bookingId);
    List<BookingResponse> getAllBookings();
    List<BookingResponse> getBookingsByUserId(Integer userId);
    BookingResponse updateBooking(Integer bookingId, BookingRequest request);
    void updateBookingStatus(Integer bookingId, BookingStatusEnum status);
}
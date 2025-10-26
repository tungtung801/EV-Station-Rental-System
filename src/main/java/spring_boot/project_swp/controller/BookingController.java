package spring_boot.project_swp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingController {

    final BookingService bookingService;

    //------------ Create Booking ----------
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    //------------ Get Booking by ID ----------
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    //------------ Get All Bookings ----------
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    //------------ Get Bookings by User ID ----------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    //------------ Update Booking ----------
    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Integer bookingId, @RequestBody @Valid BookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(bookingId, request));
    }

    //------------ Update Booking Status to CANCELLED ----------
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Integer bookingId) {
        bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CANCELLED);
        return ResponseEntity.noContent().build();
    }

    //------------ Update Booking Status to CONFIRMED ----------
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable Integer bookingId) {
        bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CONFIRMED);
        return ResponseEntity.noContent().build();
    }
}
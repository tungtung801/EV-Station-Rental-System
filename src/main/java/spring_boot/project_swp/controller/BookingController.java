package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.service.BookingService;

import java.util.List;

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

    //------------ Cancel Booking ----------
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Integer bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
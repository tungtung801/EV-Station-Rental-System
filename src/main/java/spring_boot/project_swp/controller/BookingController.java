package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Booking APIs", description = "APIs for managing bookings")
public class BookingController {

  final BookingService bookingService;

  @PostMapping
  @Operation(
      summary = "Create a new booking",
      description = "Creates a new booking with the provided details.")
  public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
    return ResponseEntity.ok(bookingService.createBooking(request));
  }

  @GetMapping("/{bookingId}")
  @Operation(summary = "Get booking by ID", description = "Retrieves a booking by its unique ID.")
  public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
    return ResponseEntity.ok(bookingService.getBookingById(bookingId));
  }

  @GetMapping
  @Operation(summary = "Get all bookings", description = "Retrieves a list of all bookings.")
  public ResponseEntity<List<BookingResponse>> getAllBookings() {
    return ResponseEntity.ok(bookingService.getAllBookings());
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "Get bookings by user ID",
      description = "Retrieves a list of bookings for a specific user.")
  public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
  }

  @PutMapping("/{bookingId}")
  @Operation(
      summary = "Update an existing booking",
      description = "Updates an existing booking with the provided details.")
  public ResponseEntity<BookingResponse> updateBooking(
      @PathVariable Long bookingId, @RequestBody @Valid BookingRequest request) {
    return ResponseEntity.ok(bookingService.updateBooking(bookingId, request));
  }

  @PutMapping("/{bookingId}/cancel")
  @Operation(summary = "Cancel a booking", description = "Cancels a booking by its ID.")
  public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
    bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CANCELLED);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{bookingId}/confirm")
  @Operation(summary = "Confirm a booking", description = "Confirms a booking by its ID.")
  public ResponseEntity<Void> confirmBooking(@PathVariable Long bookingId) {
    bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CONFIRMED);
    return ResponseEntity.noContent().build();
  }
}

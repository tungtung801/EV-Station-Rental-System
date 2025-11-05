package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.UserVerificationStatusResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.service.BookingService;
import org.springframework.context.annotation.Lazy;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Booking APIs", description = "APIs for managing bookings")
public class BookingController {

  @Lazy BookingService bookingService;

  @PostMapping
  @Operation(
      summary = "Create a new booking",
      description = "Creates a new booking with the provided details.")
  public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bookingService.createBooking(email, request));
  }

  @PatchMapping("/{bookingId}/confirm-deposit")
  @Operation(
      summary = "Confirm deposit payment for a booking",
      description = "Confirms the deposit payment for a booking and updates its status.")
  public ResponseEntity<BookingResponse> confirmDepositPayment(@PathVariable Long bookingId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    return ResponseEntity.ok(bookingService.confirmDepositPayment(bookingId, staffEmail));
  }

  @GetMapping("/{bookingId}")
  @Operation(summary = "Get booking by ID", description = "Retrieves a booking by its unique ID.")
  public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
    return ResponseEntity.ok(bookingService.getBookingById(bookingId));
  }

  @GetMapping
  @Operation(summary = "Get all bookings", description = "Retrieves a list of all bookings.")
  public ResponseEntity<List<BookingResponse>> getAllBookings(
      @RequestParam(required = false) Long stationId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    return ResponseEntity.ok(bookingService.getAllBookings(staffEmail, stationId));
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return ResponseEntity.ok(bookingService.updateBooking(bookingId, email, request));
  }

  @PatchMapping("/{bookingId}/status")
  @Operation(summary = "Update booking status", description = "Updates the status of a booking.")
  public ResponseEntity<BookingResponse> updateBookingStatus(
      @PathVariable Long bookingId, @RequestBody @Valid BookingStatusUpdateRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, email, request));
  }

  @GetMapping("/vehicle/{vehicleId}/ongoing")
  @Operation(
      summary = "Get 3 ongoing bookings of a vehicle",
      description = "Retrieves the 3 most recent ongoing bookings for a specific vehicle.")
  public ResponseEntity<List<BookingResponse>> get3OnGoingBookingsOfVehicle(
      @PathVariable Long vehicleId) {
    return ResponseEntity.ok(bookingService.get3OnGoingBookingsOfVehicle(vehicleId));
  }

  @GetMapping("/user/{userId}/verification")
  @Operation(
      summary = "Check user verification status",
      description = "Checks if a user is verified.")
  public ResponseEntity<UserVerificationStatusResponse> checkUserVerification(@PathVariable Long userId) {
      UserVerificationStatusResponse response = bookingService.checkUserVerification(userId);
      return ResponseEntity.ok(response);
  }

  @PutMapping("/{bookingId}/cancel")
  @Operation(summary = "Cancel a booking", description = "Cancels a booking by its ID.")
  public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    BookingStatusUpdateRequest request = BookingStatusUpdateRequest.builder().status(BookingStatusEnum.CANCELLED).build();
    bookingService.updateBookingStatus(bookingId, email, request);
    return ResponseEntity.noContent().build();
  }
}

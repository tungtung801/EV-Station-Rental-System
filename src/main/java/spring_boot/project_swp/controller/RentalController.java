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
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Rental APIs", description = "APIs for managing vehicle rentals")
public class RentalController {

  final RentalService rentalService;
  final UserService userService; // Để lấy ID từ Email

  // 1. Tạo Rental từ Booking
  @PostMapping("/create-from-booking/{bookingId}")
  @Operation(summary = "Create a rental from a booking")
  public ResponseEntity<RentalResponse> createRentalFromBooking(@PathVariable Long bookingId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    Long staffId = userService.getUserByEmail(staffEmail).getUserId();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(rentalService.createRentalFromBooking(bookingId, staffId));
  }

  // 2. Xác nhận Giao xe
  @PutMapping("/{rentalId}/confirm-pickup")
  @Operation(summary = "Confirm vehicle pickup")
  public ResponseEntity<RentalResponse> confirmPickup(
      @PathVariable Long rentalId, @RequestBody @Valid RentalConfirmPickupRequest request) {
    return ResponseEntity.ok(rentalService.confirmPickup(rentalId, request));
  }

  // 3. Xác nhận Trả xe (Hoàn tất)
  @PutMapping("/{rentalId}/confirm-return")
  @Operation(summary = "Confirm vehicle return")
  public ResponseEntity<RentalResponse> confirmReturn(
      @PathVariable Long rentalId, @RequestParam Long returnStationId) { // Cần trạm trả xe

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    Long staffId = userService.getUserByEmail(staffEmail).getUserId();

    return ResponseEntity.ok(rentalService.returnVehicle(rentalId, returnStationId, staffId));
  }

  @GetMapping("/{rentalId}")
  @Operation(summary = "Get rental by ID")
  public ResponseEntity<RentalResponse> getRentalById(@PathVariable Long rentalId) {
    return new ResponseEntity<>(rentalService.getRentalById(rentalId), HttpStatus.OK);
  }

  @GetMapping
  @Operation(summary = "Get all rentals")
  public ResponseEntity<List<RentalResponse>> getAllRentals() {
    return new ResponseEntity<>(rentalService.getAllRentals(), HttpStatus.OK);
  }
}

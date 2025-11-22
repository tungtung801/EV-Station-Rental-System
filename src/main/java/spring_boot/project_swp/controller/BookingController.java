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
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Booking APIs", description = "APIs for managing bookings")
public class BookingController {

  BookingService bookingService;

  // 1. TẠO BOOKING
  @PostMapping
  @Operation(summary = "Create a new booking")
  public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bookingService.createBooking(email, request));
  }

  // 2. LẤY CHI TIẾT
  @GetMapping("/{bookingId}")
  @Operation(summary = "Get booking by ID")
  public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
    return ResponseEntity.ok(bookingService.getBookingById(bookingId));
  }

  // 3. LẤY ALL (Cho Admin/Staff)
  @GetMapping
  @Operation(summary = "Get all bookings (Filter by Station optional)")
  public ResponseEntity<List<BookingResponse>> getAllBookings(
      @RequestParam(required = false) Long stationId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    return ResponseEntity.ok(bookingService.getAllBookings(staffEmail, stationId));
  }

  // 4. LẤY DANH SÁCH CỦA TÔI
  @GetMapping("/user/{userId}")
  @Operation(summary = "Get bookings by user ID")
  public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
  }

  // 5. UPDATE TRẠNG THÁI (Duyệt/Hoàn thành)
  @PatchMapping("/{bookingId}/status")
  @Operation(summary = "Update booking status (Approve/Complete)")
  public ResponseEntity<BookingResponse> updateBookingStatus(
      @PathVariable Long bookingId, @RequestBody @Valid BookingStatusUpdateRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, email, request));
  }

  // 6. HỦY ĐƠN (API tiện ích)
  @PutMapping("/{bookingId}/cancel")
  @Operation(summary = "Cancel a booking")
  public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    // Tự tạo request Hủy
    BookingStatusUpdateRequest request =
        BookingStatusUpdateRequest.builder().status(BookingStatusEnum.CANCELLED).build();

    bookingService.updateBookingStatus(bookingId, email, request);
    return ResponseEntity.noContent().build();
  }

  // 7. LẤY LỊCH XE (Để hiển thị trên UI)
  @GetMapping("/vehicle/{vehicleId}/ongoing")
  @Operation(summary = "Get ongoing bookings of a vehicle")
  public ResponseEntity<List<BookingResponse>> get3OnGoingBookingsOfVehicle(
      @PathVariable Long vehicleId) {
    return ResponseEntity.ok(bookingService.get3OnGoingBookingsOfVehicle(vehicleId));
  }


    // 1. API CHO KHÁCH HÀNG (My Bookings)
    @GetMapping("/my-bookings")
    @Operation(summary = "Get booking history of current user (Customer)")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    // 2. API CHO ADMIN (Vehicle Schedule)
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get full schedule of a vehicle (Admin/Staff)")
    // Anh có thể thêm @PreAuthorize("hasAnyAuthority('Admin', 'Staff')") nếu muốn chặt chẽ
    public ResponseEntity<List<BookingResponse>> getVehicleSchedule(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(bookingService.getVehicleSchedule(vehicleId));
    }
}

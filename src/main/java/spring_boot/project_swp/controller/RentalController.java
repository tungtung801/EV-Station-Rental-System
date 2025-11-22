package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Nhớ import cái này
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.request.RentalReturnRequest; // Nhớ import
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Rental APIs", description = "APIs for managing vehicle rentals (Staff)")
public class RentalController {

    final RentalService rentalService;
    final UserService userService;

    // Helper lấy Staff ID từ Token
    private Long getCurrentStaffId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByEmail(authentication.getName()).getUserId();
    }

    // 1. Tạo Rental thủ công (Nếu cần)
    @PostMapping("/create-from-booking/{bookingId}")
    @Operation(summary = "Create a rental manually from a booking (Staff)")
    public ResponseEntity<RentalResponse> createRentalFromBooking(@PathVariable Long bookingId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalService.createRentalFromBooking(bookingId, getCurrentStaffId()));
    }

    // 2. Xác nhận Giao xe (CÓ UPLOAD ẢNH -> Dùng @ModelAttribute)
    @PutMapping(value = "/{rentalId}/confirm-pickup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Confirm vehicle pickup (Upload Contract Image)")
    public ResponseEntity<RentalResponse> confirmPickup(
            @PathVariable Long rentalId,
            @ModelAttribute @Valid RentalConfirmPickupRequest request) { // Sửa @RequestBody -> @ModelAttribute

        return ResponseEntity.ok(rentalService.confirmPickup(rentalId, getCurrentStaffId(), request));
    }

    // 3. Xác nhận Trả xe (Dùng JSON -> Dùng @RequestBody)
    @PutMapping("/{rentalId}/confirm-return")
    @Operation(summary = "Confirm vehicle return (Input Odometer & Surcharge)")
    public ResponseEntity<RentalResponse> confirmReturn(
            @PathVariable Long rentalId,
            @RequestParam Long returnStationId,
            @RequestBody @Valid RentalReturnRequest request) { // Thêm tham số này vào

        return ResponseEntity.ok(rentalService.returnVehicle(rentalId, returnStationId, getCurrentStaffId(), request));
    }

    @GetMapping("/{rentalId}")
    @Operation(summary = "Get rental by ID")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable Long rentalId) {
        return ResponseEntity.ok(rentalService.getRentalById(rentalId));
    }

    @GetMapping
    @Operation(summary = "Get all rentals")
    public ResponseEntity<List<RentalResponse>> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }
}
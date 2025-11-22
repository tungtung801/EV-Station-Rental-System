package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.request.RentalReturnRequest;
import spring_boot.project_swp.dto.response.RentalResponse;

public interface RentalService {
  // 1. Tạo Rental khi Booking được xác nhận (Admin/Staff bấm nút "Tạo phiếu thuê")
  RentalResponse createRentalFromBooking(Long bookingId, Long staffId);

  // 1a. Tạo Rental tự động khi payment thành công (không cần staffId - dùng Admin user)
  RentalResponse createRentalFromBookingAuto(Long bookingId);

  // 2. Xác nhận giao xe (Khách đến lấy xe, Staff up ảnh hợp đồng)
  RentalResponse confirmPickup(Long rentalId, Long staffId, RentalConfirmPickupRequest request);

  // 3. Trả xe (Staff xác nhận xe đã về trạm)
  RentalResponse returnVehicle(Long rentalId, Long returnStationId, Long staffId, RentalReturnRequest request);

  RentalResponse getRentalById(Long rentalId);

  List<RentalResponse> getAllRentals();
}

package spring_boot.project_swp.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.BookingTypeEnum;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByUserUserId(Long userId);

  // Tìm booking theo trạm (cho Staff)
  List<Booking> findByVehicle_Station_StationId(Long stationId);

  // 1. Query check trùng lịch (Conflict)
  // Logic: Tìm những booking CÓ dính dáng đến khoảng thời gian [start, end]
  // VÀ trạng thái KHÔNG PHẢI là Cancelled (tức là booking đó vẫn còn hiệu lực)
  boolean existsByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNot(
      Long vehicleId, LocalDateTime endTime, LocalDateTime startTime, BookingStatusEnum status);

  // 2. Query lấy 3 booking sắp tới (để hiện lịch)
  // Logic: Lấy top 3 booking có EndTime > hiện tại (chưa kết thúc) VÀ không bị hủy
  List<Booking> findTop3ByVehicleVehicleIdAndStatusNotAndEndTimeAfterOrderByStartTimeAsc(
      Long vehicleId, BookingStatusEnum status, LocalDateTime now);

  // 1. Tìm đơn Online quá hạn thanh toán (Dựa vào CreatedAt)
  List<Booking> findByStatusAndBookingTypeAndCreatedAtBefore(
      BookingStatusEnum status, BookingTypeEnum bookingType, LocalDateTime time);

  // 2. Tìm đơn Offline (hoặc Online đã Confirm) nhưng quá giờ nhận xe mà chưa đến lấy (Dựa vào
  // StartTime)
  // Logic: Status là PENDING hoặc CONFIRMED, nhưng StartTime đã trôi qua X tiếng
  List<Booking> findByStatusInAndStartTimeBefore(
      List<BookingStatusEnum> statuses, LocalDateTime time);

  // 3. Tìm đơn theo status và StartTime (cho NO-SHOW detection)
  List<Booking> findByStatusAndStartTimeBefore(
      BookingStatusEnum status, LocalDateTime time);
    // 1. CHO KHÁCH HÀNG: Xem lịch sử của chính mình
    // Sắp xếp: Đơn mới nhất (vừa đặt) lên đầu (OrderByCreatedAtDesc)
    List<Booking> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // 2. CHO ADMIN: Xem lịch của một chiếc xe cụ thể
    // Logic: Lấy tất cả đơn của xe này TRỪ đơn đã HỦY (Cancelled)
    // Để Admin biết xe này bận những ngày nào
    List<Booking> findByVehicle_VehicleIdAndStatusNot(Long vehicleId, BookingStatusEnum status);
}

package spring_boot.project_swp.config;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.BookingTypeEnum;
import spring_boot.project_swp.repository.BookingRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BookingCleanupScheduler {

  final BookingRepository bookingRepository;

  @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
  public void cleanupBookings() {
    log.info(">>> [SCHEDULER] Bắt đầu dọn dẹp Booking...");

    // ---------------------------------------------------------
    // CASE 1: Đơn ONLINE tạo xong nhưng không thanh toán (TREO)
    // ---------------------------------------------------------
    // Quá 5 phút kể từ lúc tạo và vẫn PENDING (chưa thanh toán)
    LocalDateTime paymentDeadline = LocalDateTime.now().minusMinutes(5);

    List<Booking> unpaidOnlineBookings =
        bookingRepository.findByStatusAndBookingTypeAndCreatedAtBefore(
            BookingStatusEnum.PENDING, BookingTypeEnum.ONLINE, paymentDeadline);

    for (Booking b : unpaidOnlineBookings) {
      b.setStatus(BookingStatusEnum.CANCELLED);
      bookingRepository.save(b);
      log.info("-> Hủy đơn Online chưa thanh toán: ID {}", b.getBookingId());
    }

    // ---------------------------------------------------------
    // CASE 2: Đơn (OFFLINE hoặc Online đã thanh toán) nhưng KHÁCH KHÔNG ĐẾN LẤY (NO-SHOW)
    // ---------------------------------------------------------
    // Quá giờ nhận xe (StartTime) 2 tiếng mà vẫn chưa chuyển sang IN_PROGRESS
    // Chỉ check CONFIRMED (đã thanh toán/duyệt) để tránh cancel ONLINE chưa thanh toán 2 lần
    LocalDateTime pickupDeadline = LocalDateTime.now().minusHours(2);

    List<Booking> noShowBookings =
        bookingRepository.findByStatusAndStartTimeBefore(
            BookingStatusEnum.CONFIRMED, pickupDeadline);

    for (Booking b : noShowBookings) {
      b.setStatus(BookingStatusEnum.CANCELLED);
      // Có thể thêm logic phạt tiền ở đây nếu muốn (với đơn đã trả tiền)
      bookingRepository.save(b);
      log.info("-> Hủy đơn No-Show (Quá giờ nhận xe): ID {}", b.getBookingId());
    }

    log.info("<<< [SCHEDULER] Kết thúc dọn dẹp.");
  }
}

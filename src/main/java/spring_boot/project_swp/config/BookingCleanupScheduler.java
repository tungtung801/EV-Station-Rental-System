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
import spring_boot.project_swp.repository.BookingRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BookingCleanupScheduler {

  final BookingRepository bookingRepository;

  // Chạy mỗi 5 phút để kiểm tra và hủy các booking PENDING_PAYMENT quá hạn
  @Scheduled(fixedRate = 300000) // 300000 ms = 5 phút
  public void cleanupPendingBookings() {
    log.info("Bắt đầu chạy tác vụ dọn dẹp booking PENDING_PAYMENT...");

    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
    List<Booking> pendingBookings =
        bookingRepository.findByStatusAndCreatedAtBefore(
            BookingStatusEnum.PENDING_DEPOSIT, fiveMinutesAgo);

    if (pendingBookings.isEmpty()) {
      log.info("Không tìm thấy booking PENDING_PAYMENT nào quá hạn.");
      return;
    }

    log.info(
        "Tìm thấy {} booking PENDING_PAYMENT quá hạn. Đang tiến hành hủy...",
        pendingBookings.size());

    for (Booking booking : pendingBookings) {
      booking.setStatus(BookingStatusEnum.CANCELLED);
      bookingRepository.save(booking);
      log.info("Booking với ID: {} đã được hủy do quá hạn thanh toán.", booking.getBookingId());
    }

    log.info("Hoàn thành tác vụ dọn dẹp booking PENDING_PAYMENT.");
  }
}

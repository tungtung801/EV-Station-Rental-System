package spring_boot.project_swp.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByUserUserId(Long userId);

  List<Booking> findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
      Long vehicleId,
      LocalDateTime endTime,
      LocalDateTime startTime,
      List<BookingStatusEnum> statuses);

  List<Booking> findTop3ByVehicleVehicleIdAndStatusAndEndTimeGreaterThanOrderByStartTimeAsc(
      Long vehicleId, BookingStatusEnum status, LocalDateTime now);
}


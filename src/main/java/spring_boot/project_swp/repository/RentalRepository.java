package spring_boot.project_swp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.RentalStatusEnum;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

  List<Rental> findByRenter_UserId(Long userId);

  List<Rental> findByVehicle_VehicleId(Long vehicleId);

  Optional<Rental> findByBooking_BookingId(Long bookingId);

  List<Rental> findByVehicleVehicleIdAndStartActualBeforeAndEndActualAfterAndStatusNotIn(
      Long vehicleId,
      LocalDateTime endActual,
      LocalDateTime startActual,
      List<RentalStatusEnum> statuses);
    boolean existsByBookingBookingId(Long bookingId);
}

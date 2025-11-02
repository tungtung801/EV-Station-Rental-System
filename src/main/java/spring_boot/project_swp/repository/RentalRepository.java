package spring_boot.project_swp.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.RentalStatusEnum;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

  List<Rental> findByRenter_UserId(Long renterId);

  List<Rental> findByVehicle_VehicleId(Long vehicleId);

  List<Rental> findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
      Long vehicleId,
      LocalDateTime endTime,
      LocalDateTime startTime,
      List<RentalStatusEnum> statuses);
}

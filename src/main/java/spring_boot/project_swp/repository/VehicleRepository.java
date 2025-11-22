package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  boolean existsByLicensePlate(String licensePlate); // Dùng boolean cho gọn

  Optional<Vehicle> findByLicensePlate(String licensePlate);

  List<Vehicle> findByStation_StationIdAndVehicleStatus(Long stationId, spring_boot.project_swp.entity.VehicleStatusEnum vehicleStatus);

  List<Vehicle> findByVehicleModel_ModelId(Long modelId);
}

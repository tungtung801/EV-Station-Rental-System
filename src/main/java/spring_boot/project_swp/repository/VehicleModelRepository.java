package spring_boot.project_swp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.VehicleModel;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
  boolean existsByModelName(String modelName); // Dùng boolean cho gọn

  Optional<VehicleModel> findByModelName(String modelName);
}

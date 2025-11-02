package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.VehicleModel;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
  public List<VehicleModel> findAll();

  public Optional<VehicleModel> findByModelId(Long id);

  public Optional<VehicleModel> findByModelName(String modelName);
}

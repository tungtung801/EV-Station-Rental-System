package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.VehicleModel;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel,Integer> {
    public List<VehicleModel> findAll();
    public VehicleModel findByModelId(int id);
}

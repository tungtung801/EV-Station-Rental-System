package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.VehicleModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel,Integer> {
    public List<VehicleModel> findAll();
    public Optional<VehicleModel> findByModelId(int id);
    public Optional<VehicleModel> findByModelName(String modelName);}

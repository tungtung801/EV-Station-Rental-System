package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.VehicleChecks;

@Repository
public interface VehicleCheckRepository extends JpaRepository<VehicleChecks, Long> {}

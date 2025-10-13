package spring_boot.project_swp.repository;

import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import spring_boot.project_swp.entity.Vehicle;

@Registered
public interface VehicleRepository extends JpaRepository<Vehicle,Integer> {
}

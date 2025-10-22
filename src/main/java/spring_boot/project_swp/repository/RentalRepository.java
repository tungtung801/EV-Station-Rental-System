package spring_boot.project_swp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring_boot.project_swp.entity.Rental;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
    List<Rental> findByRenter_UserId(Integer renterId);
    List<Rental> findByVehicle_VehicleId(Integer vehicleId);
}
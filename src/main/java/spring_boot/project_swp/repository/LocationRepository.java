package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    public Optional<Location> findByLocationId(Integer locationId);

    public Optional<Location> findByLocationName(String locationName);

    public List<Location> findByLocationType(String locationType);

    public List<Location> findAllByIsActiveTrue();

    public List<Location> findByParentIsNotNull(); // TÌM LOCATION CON

    public List<Location> findByParentIsNull(); // TÌM LOCATION CHA

    public List<Location> findByParentLocationIdAndLocationType(Integer parentLocationId, String locationType);

}

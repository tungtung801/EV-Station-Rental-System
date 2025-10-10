package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Location;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    public Location findByLocationName(String locationName);

    public List<Location> findAllByIsActiveTrue();

    public List<Location> findByLocationTypeAndIsActiveTrueOrderByLocationNameAsc(String locationType);

    public List<Location> findByParent_LocationIdAndLocationTypeAndIsActiveTrueOrderByLocationNameAsc(Integer parentLocationId, String childLocationType);
}

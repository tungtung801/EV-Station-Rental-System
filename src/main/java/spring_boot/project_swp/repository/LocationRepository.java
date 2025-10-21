package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    public Optional<Location> findByLocationId(Integer id);
    public Optional<Location> findByLocationName(String locationName);
    public List<Location> findByIsActiveIsTrue();
    public List<Location> findByIsActiveIsFalse();
    public List<Location> findByLocationTypeAndIsActiveTrueOrderByLocationNameAsc(String locationType);
    public List<Location> findByParent_LocationIdAndLocationTypeAndIsActiveTrueOrderByLocationNameAsc(Integer parentLocationId, String childLocationType);
    public List<Location> findByParentAndLocationType(Location parent, String locationType);
    public List<Location> findByParent(Location parent);
    Optional<Location> findByLocationNameAndParent(String locationName, Location parent);
}

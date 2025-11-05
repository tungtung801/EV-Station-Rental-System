package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.StationStatusEnum;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
  public List<Station> findByIsActive(StationStatusEnum isActive);

  public Optional<Station> findStationByStationId(Long id);

  public Optional<Station> findStationByStationName(String name);

  public List<Station> findByLocation_LocationIdAndIsActive(
      Long locationId, StationStatusEnum isActive);

  public List<Station> findByLocation_LocationIdInAndIsActive(
      List<Long> locationIds, StationStatusEnum isActive);
}

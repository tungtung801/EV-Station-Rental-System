package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
  public List<Station> findByIsActiveIsTrue();

  public List<Station> findByIsActiveIsFalse();

  public Optional<Station> findStationByStationId(Long id);

  public Optional<Station> findStationByStationName(String name);

  public List<Station> findByLocation_LocationIdAndIsActiveTrue(Long locationId);

  public List<Station> findByLocation_LocationIdInAndIsActiveTrue(List<Long> locationIds);

  @Query(
      "SELECT s FROM Station s WHERE s.isActive = true AND s.location.locationId IN "
          + "(SELECT w.locationId FROM Location w WHERE w.parent.locationId IN "
          + "(SELECT d.locationId FROM Location d WHERE d.parent.locationId = :cityId))")
  List<Station> findStationsByCityId(Long cityId);

  @Query(
      "SELECT s FROM Station s WHERE s.isActive = true AND s.location.locationId IN "
          + "(SELECT w.locationId FROM Location w WHERE w.parent.locationId = :districtId)")
  List<Station> findStationsByDistrictId(Long districtId);
}

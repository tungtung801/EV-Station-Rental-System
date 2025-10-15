package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Station;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {
    public List<Station> findByIsActiveIsTrue();
    public List<Station> findByIsActiveIsFalse();
    public Station findStationByStationId(Integer id);
    public Station findStationByStationName(String name);
    public List<Station> findByLocation_LocationIdAndIsActiveTrue(Integer locationId);
    public List<Station> findByLocation_LocationIdInAndIsActiveTrue(List<Integer> locationIds);

    // CỰC KÌ QUAN TRỌNG, GIÚP TI KIÉM STATION THEO TP, THEO TP + QUẬN
    @Query("SELECT s FROM Station s WHERE s.isActive = true AND s.location.locationId IN " +
            "(SELECT w.locationId FROM Location w WHERE w.parent.locationId IN " +
            "(SELECT d.locationId FROM Location d WHERE d.parent.locationId = :cityId))")
    List<Station> findStationsByCityId(Integer cityId);

    @Query("SELECT s FROM Station s WHERE s.isActive = true AND s.location.locationId IN " +
            "(SELECT w.locationId FROM Location w WHERE w.parent.locationId = :districtId)")
    List<Station> findStationsByDistrictId(Integer districtId);
}

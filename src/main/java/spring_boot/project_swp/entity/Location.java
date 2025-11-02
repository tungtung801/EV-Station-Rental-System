package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Locations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
  @Id
  @Column(name = "LocationId")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long locationId;

  @Column(name = "LocationName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
  String locationName;

  @Column(name = "LocationType", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
  String locationType;

  @Column(name = "Latitude", precision = 10, scale = 2)
  BigDecimal latitude;

  @Column(name = "Longitude", precision = 10, scale = 2)
  BigDecimal longitude;

  @Column(name = "Radius", precision = 5, scale = 2)
  BigDecimal radius;

  @Column(name = "IsActive", nullable = false)
  boolean isActive;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ParentLocationId")
  Location parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, targetEntity = Location.class)
  @JsonIgnore
  List<Location> children = new ArrayList<>();

  @OneToMany(
      mappedBy = "location",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @JsonIgnore
  List<Station> stations = new ArrayList<>();
}

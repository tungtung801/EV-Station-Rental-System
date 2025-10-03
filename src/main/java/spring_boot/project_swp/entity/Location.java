package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Locations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    @Id
    @Column(name = "LocationId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer locationId;

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

    @Column(name = "Address", nullable = true, length = 255, columnDefinition = "NVARCHAR(255)")
    String address;

    @Column(name = "IsActive", nullable = false)
    boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentLocationId")
    Location parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Thêm annotation để tránh vòng lặp vô hạn khi serialize
    List<Location> children = new ArrayList<>();

}

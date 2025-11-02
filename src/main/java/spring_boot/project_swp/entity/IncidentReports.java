package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "IncidentReports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IncidentReports {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ReportId")
  Long reportId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RentalId", nullable = false)
  Rental rental;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VehicleId", nullable = false)
  @JsonIgnore
  Vehicle vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId", nullable = false)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CheckId")
  VehicleChecks vehicleCheck;

  @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)", nullable = false)
  String description;

  @Column(name = "Status", nullable = false)
  String status;

  @CreationTimestamp
  @Column(name = "ReportDate", nullable = false, updatable = false)
  LocalDateTime reportDate;

  @Column(name = "ImageUrls", columnDefinition = "NVARCHAR(MAX)")
  String imageUrls;
}

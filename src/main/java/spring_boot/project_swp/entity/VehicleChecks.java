package spring_boot.project_swp.entity;

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
@Table(name = "VehicleChecks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VehicleChecks {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "CheckId")
  Long checkId; // ĐÃ CHUYỂN SANG LONG

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RentalId", nullable = false)
  Rental rental;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "StaffId", nullable = false)
  User staff;

  @Column(name = "CheckType", nullable = false)
  String checkType; // e.g., "Pre-rental", "Post-rental", "Maintenance"

  @CreationTimestamp
  @Column(name = "CheckDate", nullable = false, updatable = false)
  LocalDateTime checkDate;

  @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
  String notes;

  @Column(name = "ImageUrls", columnDefinition = "NVARCHAR(MAX)")
  String imageUrls; // Comma-separated URLs or JSON string

  public VehicleChecks(Long checkId) {
    this.checkId = checkId;
  }
}

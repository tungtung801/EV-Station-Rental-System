package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CheckId") // Nullable as discussed
    VehicleChecks vehicleCheck;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    String description;

    @Column(name = "Status", nullable = false)
    String status; // e.g., "Reported", "Under Investigation", "Resolved"

    @CreationTimestamp
    @Column(name = "ReportDate", nullable = false, updatable = false)
    LocalDateTime reportDate;

    @Column(name = "ImageUrls", columnDefinition = "NVARCHAR(MAX)")
    String imageUrls; // Comma-separated URLs or JSON string

}
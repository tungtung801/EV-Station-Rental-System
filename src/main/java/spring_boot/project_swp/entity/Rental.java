package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentalId")
    Integer rentalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingId", nullable = false)
    @ToString.Exclude
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RenterId", nullable = false)
    @ToString.Exclude
    User renter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleId", nullable = false)
    @ToString.Exclude
    Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PickupStationId", nullable = false)
    @ToString.Exclude
    Station pickupStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnStationId")
    @ToString.Exclude
    Station returnStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PickupStaffId")
    @ToString.Exclude
    User pickupStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnStaffId")
    @ToString.Exclude
    User returnStaff;

    @Column(name = "StartTime", nullable = false)
    LocalDateTime startTime;

    @Column(name = "EndTime")
    LocalDateTime endTime;

    @Column(name = "TotalCost", columnDefinition = "DECIMAL(10,2)")
    Double totalCost;

    @Column(name = "Status", length = 50)
    String status;

    @Column(name = "ContractUrl", length = 255)
    String contractUrl;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    LocalDateTime createdAt;

    public Rental(Long rentalId) {
        this.rentalId = rentalId.intValue();
    }

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<RentalDiscounts> rentalDiscounts;
}
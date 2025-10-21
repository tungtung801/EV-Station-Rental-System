package spring_boot.project_swp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingId")
    Integer bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @ToString.Exclude
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleId", nullable = false)
    @ToString.Exclude
    Vehicle vehicle;

    @Column(name = "BookingType", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    BookingTypeEnum bookingType;

    @Column(name = "StartTime", nullable = false)
    LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    LocalDateTime endTime;

    @Column(name = "TotalAmount", nullable = false)
    Double totalAmount;

    @Column(name = "Status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    BookingStatusEnum status = BookingStatusEnum.PENDING;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
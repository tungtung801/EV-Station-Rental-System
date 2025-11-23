package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

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
    Long rentalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingId", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User renter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Vehicle vehicle;

    // Trạm giao/nhận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PickupStationId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Station pickupStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnStationId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Station returnStation;

    // Nhân viên giao/nhận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PickupStaffId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User pickupStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnStaffId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User returnStaff;

    @Column(name = "StartActual")
    LocalDateTime startActual;

    @Column(name = "EndActual")
    LocalDateTime endActual;

    @Column(name = "Total", precision = 19, scale = 4)
    BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    @Builder.Default
    RentalStatusEnum status = RentalStatusEnum.PENDING_PICKUP;

    @Column(name = "ContractUrl", length = 500)
    String contractUrl;

    // --- [BỔ SUNG CÁC TRƯỜNG THIẾU] ---

    @Column(name = "PickupNote", length = 500)
    String pickupNote; // Ghi chú lúc giao xe

    @Column(name = "ReturnNote", length = 500)
    String returnNote; // Ghi chú lúc trả xe

    @Column(name = "StartOdometer")
    Integer startOdometer; // Số km lúc đi

    @Column(name = "EndOdometer")
    Integer endOdometer; // Số km lúc về

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnCheckId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    VehicleChecks returnCheck; // Kiểm tra xe lúc trả

    // ----------------------------------

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Payment> payments;
}
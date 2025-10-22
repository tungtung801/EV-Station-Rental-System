package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DiscountId")
    Integer discountId;

    @Column(name = "Code", length = 50, unique = true, nullable = false)
    String code;

    @Column(name = "Description", columnDefinition = "TEXT")
    String description;

    @Column(name = "AmountPercentage", precision = 5, scale = 2)
    BigDecimal amountPercentage;

    @Column(name = "AmountFixed", precision = 10, scale = 2)
    BigDecimal amountFixed;

    @Column(name = "StartDate", nullable = false)
    LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    LocalDateTime endDate;

    @Column(name = "MinRentalDuration")
    Integer minRentalDuration;

    @Column(name = "MaxDiscountAmount", precision = 10, scale = 2)
    BigDecimal maxDiscountAmount;

    @Column(name = "UsageLimit")
    Integer usageLimit;

    @Column(name = "CurrentUsage")
    Integer currentUsage = 0; // Default value

    @Column(name = "IsActive", nullable = false)
    Boolean isActive = true; // Default value

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    List<RentalDiscounts> rentalDiscounts;
}
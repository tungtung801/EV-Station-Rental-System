package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    Long discountId;

    @Column(name = "Code", length = 50, unique = true, nullable = false)
    String code; // VD: SALE50, TET2025

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)") // Sửa TEXT thành NVARCHAR để hỗ trợ tiếng Việt
    String description;

    // --- SỬA LẠI CHO KHỚP VỚI LOGIC SERVICE ---
    @Enumerated(EnumType.STRING)
    @Column(name = "DiscountType", nullable = false)
    DiscountTypeEnum discountType; // PERCENTAGE hoặc FIXED_AMOUNT

    @Column(name = "Value", nullable = false, precision = 10, scale = 2)
    BigDecimal value; // Giá trị (VD: 10 nếu là %, 50000 nếu là tiền)
    // ------------------------------------------

    @Column(name = "MaxDiscountAmount", precision = 10, scale = 2)
    BigDecimal maxDiscountAmount; // VD: Giảm 10% nhưng tối đa 200k

    @Column(name = "MinRentalDuration")
    Integer minRentalDuration; // Điều kiện: Thuê tối thiểu mấy ngày mới được dùng

    @Column(name = "UsageLimit")
    Integer usageLimit; // Tổng số lượng mã (VD: 100 mã)

    @Column(name = "CurrentUsage")
    @Builder.Default
    Integer currentUsage = 0; // Đã dùng bao nhiêu mã

    @Column(name = "StartDate", nullable = false)
    LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    LocalDateTime endDate;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    // ĐÃ XÓA List<RentalDiscounts> rentalDiscounts -> VÌ NÓ KHÔNG CÒN TỒN TẠI
}
package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "RentalDiscounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(RentalDiscountsId.class)
public class RentalDiscounts {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RentalId", referencedColumnName = "RentalId")
  @ToString.Exclude // <--- THÊM
  @EqualsAndHashCode.Exclude // <--- THÊM
  Rental rental;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "DiscountId", referencedColumnName = "DiscountId")
  @ToString.Exclude // <--- THÊM
  @EqualsAndHashCode.Exclude // <--- THÊM
  Discount discount;

  @Column(name = "AppliedAmount", precision = 10, scale = 2)
  BigDecimal appliedAmount;

  @Column(name = "AppliedAt")
  LocalDateTime appliedAt;
}

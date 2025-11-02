package spring_boot.project_swp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.RentalDiscounts;
import spring_boot.project_swp.entity.RentalDiscountsId;

@Repository
public interface RentalDiscountsRepository
    extends JpaRepository<RentalDiscounts, RentalDiscountsId> {
  List<RentalDiscounts> findByRental_RentalId(Long rentalId);

  List<RentalDiscounts> findByDiscount_DiscountId(Long discountId);
}

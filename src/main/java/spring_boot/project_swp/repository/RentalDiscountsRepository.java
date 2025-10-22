package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.RentalDiscounts;
import spring_boot.project_swp.entity.RentalDiscountsId;

import java.util.List;

@Repository
public interface RentalDiscountsRepository extends JpaRepository<RentalDiscounts, RentalDiscountsId> {
    List<RentalDiscounts> findByRental_RentalId(Integer rentalId);
    List<RentalDiscounts> findByDiscount_DiscountId(Integer discountId);
}
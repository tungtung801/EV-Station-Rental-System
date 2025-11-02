package spring_boot.project_swp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
  Optional<Discount> findByCode(String code);
}

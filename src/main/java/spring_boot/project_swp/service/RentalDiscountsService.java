package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;

public interface RentalDiscountsService {
  RentalDiscountResponse createRentalDiscount(RentalDiscountRequest request);

  RentalDiscountResponse getRentalDiscountById(Long rentalId, Long discountId);

  List<RentalDiscountResponse> getAllRentalDiscounts();

  List<RentalDiscountResponse> getRentalDiscountsByRentalId(Long rentalId);

  List<RentalDiscountResponse> getRentalDiscountsByDiscountId(Long discountId);

  void deleteRentalDiscount(Long rentalId, Long discountId);

  RentalDiscountResponse updateRentalDiscount(RentalDiscountRequest request);
}

package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;

public interface DiscountService {
  DiscountResponse createDiscount(DiscountRequest request);

  DiscountResponse getDiscountById(Long discountId);

  List<DiscountResponse> getAllDiscounts();

  DiscountResponse updateDiscount(Long discountId, DiscountRequest request);

  void deleteDiscount(Long discountId);

  DiscountResponse getDiscountByCode(String code);
}

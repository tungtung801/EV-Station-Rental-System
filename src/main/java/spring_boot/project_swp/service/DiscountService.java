package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;

public interface DiscountService {
  DiscountResponse createDiscount(DiscountRequest request);

  List<DiscountResponse> getAllDiscounts();

  DiscountResponse getDiscountByCode(String code);

  // BỔ SUNG 2 CÁI NÀY
  DiscountResponse getDiscountById(Long discountId);

  DiscountResponse updateDiscount(Long discountId, DiscountRequest request);

  void deleteDiscount(Long id);
}

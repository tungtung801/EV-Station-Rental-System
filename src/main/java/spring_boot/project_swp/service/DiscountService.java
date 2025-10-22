package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;

import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountRequest request);
    DiscountResponse getDiscountById(Integer discountId);
    List<DiscountResponse> getAllDiscounts();
    DiscountResponse updateDiscount(Integer discountId, DiscountRequest request);
    void deleteDiscount(Integer discountId);
    DiscountResponse getDiscountByCode(String code);
}
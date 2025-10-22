package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;

import java.util.List;

public interface RentalDiscountsService {
    RentalDiscountResponse createRentalDiscount(RentalDiscountRequest request);
    RentalDiscountResponse getRentalDiscountById(Integer rentalId, Integer discountId);
    List<RentalDiscountResponse> getAllRentalDiscounts();
    List<RentalDiscountResponse> getRentalDiscountsByRentalId(Integer rentalId);
    List<RentalDiscountResponse> getRentalDiscountsByDiscountId(Integer discountId);
    void deleteRentalDiscount(Integer rentalId, Integer discountId);
}
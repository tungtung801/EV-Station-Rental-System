package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;
import spring_boot.project_swp.entity.Discount;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DiscountMapper {

  Discount toDiscount(DiscountRequest request);

  DiscountResponse toDiscountResponse(Discount discount);

  @Mapping(target = "discountId", ignore = true)
  @Mapping(target = "currentUsage", ignore = true)
  @Mapping(target = "rentalDiscounts", ignore = true)
  void updateDiscountFromRequest(DiscountRequest request, @MappingTarget Discount discount);
}

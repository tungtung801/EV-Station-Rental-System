package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;
import spring_boot.project_swp.entity.RentalDiscounts;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalDiscountsMapper {

  @Mapping(target = "rental", ignore = true) // Set trong Service
  @Mapping(target = "discount", ignore = true) // Set trong Service
  RentalDiscounts toRentalDiscounts(RentalDiscountRequest request);

  @Mapping(target = "rentalId", source = "rental.rentalId")
  @Mapping(target = "discountId", source = "discount.discountId")
  RentalDiscountResponse toRentalDiscountResponse(RentalDiscounts rentalDiscounts);

  @Mapping(target = "rental", ignore = true)
  @Mapping(target = "discount", ignore = true)
  void updateRentalDiscountsFromRequest(
      RentalDiscountRequest request, @MappingTarget RentalDiscounts rentalDiscounts);
}

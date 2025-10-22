package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;
import spring_boot.project_swp.entity.RentalDiscounts;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.Discount;

@Mapper(componentModel = "spring")
public interface RentalDiscountsMapper {

    @Mapping(source = "rentalId", target = "rental.rentalId")
    @Mapping(source = "discountId", target = "discount.discountId")
    RentalDiscounts toRentalDiscounts(RentalDiscountRequest request);

    @Mapping(source = "rental.rentalId", target = "rentalId")
    @Mapping(source = "discount.discountId", target = "discountId")
    RentalDiscountResponse toRentalDiscountResponse(RentalDiscounts rentalDiscounts);

    @Mapping(target = "rental", ignore = true)
    @Mapping(target = "discount", ignore = true)
    void updateRentalDiscountsFromRequest(RentalDiscountRequest request, @MappingTarget RentalDiscounts rentalDiscounts);

    default Rental mapRentalIdToRental(Integer rentalId) {
        if (rentalId == null) {
            return null;
        }
        return Rental.builder().rentalId(rentalId).build();
    }

    default Discount mapDiscountIdToDiscount(Integer discountId) {
        if (discountId == null) {
            return null;
        }
        return Discount.builder().discountId(discountId).build();
    }
}
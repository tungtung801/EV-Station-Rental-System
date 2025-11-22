package spring_boot.project_swp.mapper;

import java.util.List; // Nhớ import List
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

    // --- CREATE ---
    @Mapping(target = "discountId", ignore = true)
    @Mapping(target = "currentUsage", ignore = true) // Mặc định 0
    // ĐÃ XÓA DÒNG rentalDiscounts VÌ FIELD NÀY KHÔNG CÒN TỒN TẠI
    Discount toDiscount(DiscountRequest request);

    // --- RESPONSE ---
    DiscountResponse toDiscountResponse(Discount discount);

    // Thêm hàm này để Controller gọi API Get All
    List<DiscountResponse> toDiscountResponseList(List<Discount> discounts);

    // --- UPDATE ---
    @Mapping(target = "discountId", ignore = true)
    @Mapping(target = "currentUsage", ignore = true) // Không cho update số lượt dùng qua API này
    void updateDiscountFromRequest(DiscountRequest request, @MappingTarget Discount discount);
}
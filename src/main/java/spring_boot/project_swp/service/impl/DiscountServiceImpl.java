package spring_boot.project_swp.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;
import spring_boot.project_swp.entity.Discount;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.DiscountMapper;
import spring_boot.project_swp.repository.DiscountRepository;
import spring_boot.project_swp.service.DiscountService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountServiceImpl implements DiscountService {

  final DiscountRepository discountRepository;
  final DiscountMapper discountMapper;

  @Override
  public DiscountResponse createDiscount(DiscountRequest request) {
    if (discountRepository.findByCode(request.getCode()).isPresent()) {
      throw new ConflictException("Discount code already exists");
    }
    Discount discount = discountMapper.toDiscount(request);
    return discountMapper.toDiscountResponse(discountRepository.save(discount));
  }

  @Override
  public DiscountResponse getDiscountById(Long discountId) {
    Discount discount =
        discountRepository
            .findById(discountId)
            .orElseThrow(() -> new NotFoundException("Discount not found with ID: " + discountId));
    return discountMapper.toDiscountResponse(discount);
  }

  @Override
  public List<DiscountResponse> getAllDiscounts() {
    List<Discount> discounts = discountRepository.findAll();
    List<DiscountResponse> discountResponses = new java.util.ArrayList<>();
    for (Discount discount : discounts) {
      discountResponses.add(discountMapper.toDiscountResponse(discount));
    }
    return discountResponses;
  }

  @Override
  public DiscountResponse updateDiscount(Long discountId, DiscountRequest request) {
    Discount existingDiscount =
        discountRepository
            .findById(discountId)
            .orElseThrow(() -> new NotFoundException("Discount not found with ID: " + discountId));

    Optional<Discount> discountWithSameCode = discountRepository.findByCode(request.getCode());
    if (discountWithSameCode.isPresent()
        && !discountWithSameCode.get().getDiscountId().equals(discountId)) {
      throw new ConflictException("Discount code already exists");
    }

    discountMapper.updateDiscountFromRequest(request, existingDiscount);
    return discountMapper.toDiscountResponse(discountRepository.save(existingDiscount));
  }

  @Override
  public void deleteDiscount(Long discountId) {
    if (!discountRepository.existsById(discountId)) {
      throw new NotFoundException("Discount not found with ID: " + discountId);
    }
    discountRepository.deleteById(discountId);
  }

  @Override
  public DiscountResponse getDiscountByCode(String code) {
    Discount discount =
        discountRepository
            .findByCode(code)
            .orElseThrow(() -> new NotFoundException("Discount not found with Code: " + code));
    return discountMapper.toDiscountResponse(discount);
  }
}

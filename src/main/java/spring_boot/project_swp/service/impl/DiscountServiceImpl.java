package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  @Transactional
  public DiscountResponse createDiscount(DiscountRequest request) {
    if (discountRepository.findByCode(request.getCode()).isPresent()) {
      throw new ConflictException("Discount code already exists");
    }
    Discount discount = discountMapper.toDiscount(request);
    return discountMapper.toDiscountResponse(discountRepository.save(discount));
  }

  @Override
  public List<DiscountResponse> getAllDiscounts() {
    List<Discount> discounts = discountRepository.findAll();
    List<DiscountResponse> responses = new ArrayList<>();
    for (Discount d : discounts) {
      responses.add(discountMapper.toDiscountResponse(d));
    }
    return responses;
  }

  @Override
  public DiscountResponse getDiscountByCode(String code) {
    Discount discount =
        discountRepository
            .findByCode(code)
            .orElseThrow(() -> new NotFoundException("Discount not found: " + code));
    return discountMapper.toDiscountResponse(discount);
  }

  // --- BỔ SUNG: GET BY ID ---
  @Override
  public DiscountResponse getDiscountById(Long discountId) {
    Discount discount =
        discountRepository
            .findById(discountId)
            .orElseThrow(() -> new NotFoundException("Discount not found with ID: " + discountId));
    return discountMapper.toDiscountResponse(discount);
  }

  // --- BỔ SUNG: UPDATE ---
  @Override
  @Transactional
  public DiscountResponse updateDiscount(Long discountId, DiscountRequest request) {
    Discount discount =
        discountRepository
            .findById(discountId)
            .orElseThrow(() -> new NotFoundException("Discount not found with ID: " + discountId));

    // Check trùng code (nếu đổi code khác)
    if (!discount.getCode().equals(request.getCode())
        && discountRepository.findByCode(request.getCode()).isPresent()) {
      throw new ConflictException("Discount code already exists");
    }

    discountMapper.updateDiscountFromRequest(request, discount);
    return discountMapper.toDiscountResponse(discountRepository.save(discount));
  }

  @Override
  @Transactional
  public void deleteDiscount(Long id) {
    if (!discountRepository.existsById(id)) {
      throw new NotFoundException("Discount not found");
    }
    discountRepository.deleteById(id);
  }
}

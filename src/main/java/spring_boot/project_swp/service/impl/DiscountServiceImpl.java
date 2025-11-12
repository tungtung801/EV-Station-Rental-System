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
    // Validate discount code is unique
    if (discountRepository.findByCode(request.getCode()).isPresent()) {
      throw new ConflictException("Discount code already exists");
    }

    // Validate date range
    validateDiscountDates(request.getStartDate(), request.getEndDate());

    // Validate discount type and amounts
    validateDiscountAmounts(request);

    // Validate usage limit
    if (request.getUsageLimit() != null && request.getUsageLimit() <= 0) {
      throw new ConflictException("Usage limit must be greater than 0");
    }

    // Validate min rental duration
    if (request.getMinRentalDuration() != null && request.getMinRentalDuration() <= 0) {
      throw new ConflictException("Minimum rental duration must be greater than 0");
    }

    Discount discount = discountMapper.toDiscount(request);
    discount.setCurrentUsage(0); // Initialize current usage
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

    // Validate discount code is unique (except for current discount)
    Optional<Discount> discountWithSameCode = discountRepository.findByCode(request.getCode());
    if (discountWithSameCode.isPresent()
        && !discountWithSameCode.get().getDiscountId().equals(discountId)) {
      throw new ConflictException("Discount code already exists");
    }

    // Validate date range
    validateDiscountDates(request.getStartDate(), request.getEndDate());

    // Validate discount type and amounts
    validateDiscountAmounts(request);

    // Validate usage limit
    if (request.getUsageLimit() != null && request.getUsageLimit() <= 0) {
      throw new ConflictException("Usage limit must be greater than 0");
    }

    // Validate usage limit is not less than current usage
    if (request.getUsageLimit() != null
        && existingDiscount.getCurrentUsage() != null
        && request.getUsageLimit() < existingDiscount.getCurrentUsage()) {
      throw new ConflictException(
          "Usage limit cannot be less than current usage count ("
          + existingDiscount.getCurrentUsage() + ")");
    }

    // Validate min rental duration
    if (request.getMinRentalDuration() != null && request.getMinRentalDuration() <= 0) {

      throw new ConflictException("Minimum rental duration must be greater than 0");
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

  // ======================== HELPER METHODS ========================

  /**
   * Validate discount dates
   */
  private void validateDiscountDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
    if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
      throw new ConflictException("End date must be after start date");
    }

    // Optional: Kiểm tra discount không được quá dài (ví dụ: tối đa 1 năm)
    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    if (daysBetween > 365) {
      throw new ConflictException("Discount period cannot exceed 365 days");
    }
  }

  /**
   * Validate discount amounts and types
   */
  private void validateDiscountAmounts(DiscountRequest request) {
    // Phải có ít nhất 1 loại discount (percentage hoặc fixed)
    if ((request.getAmountPercentage() == null || request.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) == 0)
        && (request.getAmountFixed() == null || request.getAmountFixed().compareTo(java.math.BigDecimal.ZERO) == 0)) {
      throw new ConflictException("Must have either percentage discount or fixed discount amount");
    }

    // Không được có cả 2 loại discount cùng lúc
    if (request.getAmountPercentage() != null
        && request.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) > 0
        && request.getAmountFixed() != null
        && request.getAmountFixed().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new ConflictException("Cannot have both percentage and fixed discount at the same time");
    }

    // Validate percentage discount
    if (request.getAmountPercentage() != null && request.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
      if (request.getAmountPercentage().compareTo(new java.math.BigDecimal("100")) > 0) {
        throw new ConflictException("Percentage discount cannot exceed 100%");
      }
      if (request.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new ConflictException("Percentage discount must be greater than 0");
      }
    }

    // Validate fixed discount
    if (request.getAmountFixed() != null && request.getAmountFixed().compareTo(java.math.BigDecimal.ZERO) > 0) {
      if (request.getAmountFixed().compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new ConflictException("Fixed discount amount must be greater than 0");
      }
    }

    // Validate max discount amount (chỉ áp dụng cho percentage discount)
    if (request.getMaxDiscountAmount() != null) {
      if (request.getMaxDiscountAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
        throw new ConflictException("Maximum discount amount must be greater than 0");
      }
      // Max discount chỉ có ý nghĩa với percentage discount
      if (request.getAmountPercentage() == null || request.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) == 0) {
        throw new ConflictException("Maximum discount amount is only applicable for percentage discounts");
      }
    }
  }

  /**
   * Check if discount can be applied (kiểm tra các điều kiện: active, date range, usage limit)
   */
  public boolean canApplyDiscount(String code) {
    Optional<Discount> discountOpt = discountRepository.findByCode(code);
    if (discountOpt.isEmpty()) {
      return false;
    }

    Discount discount = discountOpt.get();
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    // Kiểm tra discount có active không
    if (!discount.getIsActive()) {
      return false;
    }

    // Kiểm tra thời gian
    if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
      return false;
    }

    // Kiểm tra usage limit
    if (discount.getUsageLimit() != null
        && discount.getCurrentUsage() >= discount.getUsageLimit()) {
      return false;
    }

    return true;
  }

  /**
   * Get all active discounts (trong thời gian hiệu lực và còn usage limit)
   */
  public List<DiscountResponse> getActiveDiscounts() {
    List<Discount> discounts = discountRepository.findAll();
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    return discounts.stream()
        .filter(d -> d.getIsActive())
        .filter(d -> !now.isBefore(d.getStartDate()) && !now.isAfter(d.getEndDate()))
        .filter(d -> d.getUsageLimit() == null || d.getCurrentUsage() < d.getUsageLimit())
        .map(discountMapper::toDiscountResponse)
        .toList();
  }

  /**
   * Calculate discount amount for a given rental amount and duration
   */
  public java.math.BigDecimal calculateDiscountAmount(
      String code,
      java.math.BigDecimal rentalAmount,
      Integer rentalDurationDays) {

    Discount discount = discountRepository
        .findByCode(code)
        .orElseThrow(() -> new NotFoundException("Discount not found with Code: " + code));

    // Kiểm tra có thể áp dụng không
    if (!canApplyDiscount(code)) {
      throw new ConflictException("Discount cannot be applied");
    }

    // Kiểm tra min rental duration
    if (discount.getMinRentalDuration() != null
        && rentalDurationDays < discount.getMinRentalDuration()) {
      throw new ConflictException(
          "Rental duration must be at least " + discount.getMinRentalDuration() + " days");
    }

    java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

    // Tính toán discount amount
    if (discount.getAmountPercentage() != null
        && discount.getAmountPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
      // Percentage discount
      discountAmount = rentalAmount
          .multiply(discount.getAmountPercentage())
          .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

      // Áp dụng max discount nếu có
      if (discount.getMaxDiscountAmount() != null
          && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
        discountAmount = discount.getMaxDiscountAmount();
      }
    } else if (discount.getAmountFixed() != null
        && discount.getAmountFixed().compareTo(java.math.BigDecimal.ZERO) > 0) {
      // Fixed discount
      discountAmount = discount.getAmountFixed();

      // Discount không được lớn hơn rental amount
      if (discountAmount.compareTo(rentalAmount) > 0) {
        discountAmount = rentalAmount;
      }
    }

    return discountAmount;
  }

  /**
   * Increment usage count when discount is applied
   */
  public void incrementUsage(String code) {
    Discount discount = discountRepository
        .findByCode(code)
        .orElseThrow(() -> new NotFoundException("Discount not found with Code: " + code));

    if (discount.getCurrentUsage() == null) {
      discount.setCurrentUsage(1);
    } else {
      discount.setCurrentUsage(discount.getCurrentUsage() + 1);
    }

    discountRepository.save(discount);
  }

  /**
   * Deactivate expired discounts (có thể chạy định kỳ bằng scheduled task)
   */
  public void deactivateExpiredDiscounts() {
    List<Discount> discounts = discountRepository.findAll();
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    discounts.stream()
        .filter(d -> d.getIsActive() && now.isAfter(d.getEndDate()))
        .forEach(d -> {
          d.setIsActive(false);
          discountRepository.save(d);
        });
  }
}

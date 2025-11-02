package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;
import spring_boot.project_swp.entity.Discount;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.RentalDiscounts;
import spring_boot.project_swp.entity.RentalDiscountsId;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RentalDiscountsMapper;
import spring_boot.project_swp.repository.DiscountRepository;
import spring_boot.project_swp.repository.RentalDiscountsRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.service.RentalDiscountsService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalDiscountsServiceImpl implements RentalDiscountsService {

  final RentalDiscountsRepository rentalDiscountsRepository;
  final RentalRepository rentalRepository;
  final DiscountRepository discountRepository;
  final RentalDiscountsMapper rentalDiscountsMapper;

  @Override
  public RentalDiscountResponse createRentalDiscount(RentalDiscountRequest request) {
    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy Rental với ID: " + request.getRentalId()));
    Discount discount =
        discountRepository
            .findById(request.getDiscountId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy Discount với ID: " + request.getDiscountId()));

    RentalDiscountsId id = new RentalDiscountsId(request.getRentalId(), request.getDiscountId());
    if (rentalDiscountsRepository.findById(id).isPresent()) {
      throw new ConflictException(
          "RentalDiscount đã tồn tại cho Rental ID: "
              + request.getRentalId()
              + " và Discount ID: "
              + request.getDiscountId());
    }

    RentalDiscounts rentalDiscounts = rentalDiscountsMapper.toRentalDiscounts(request);
    rentalDiscounts.setRental(rental);
    rentalDiscounts.setDiscount(discount);

    return rentalDiscountsMapper.toRentalDiscountResponse(
        rentalDiscountsRepository.save(rentalDiscounts));
  }

  @Override
  public RentalDiscountResponse getRentalDiscountById(Long rentalId, Long discountId) {
    RentalDiscountsId id = new RentalDiscountsId(rentalId, discountId);
    RentalDiscounts rentalDiscounts =
        rentalDiscountsRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy RentalDiscount với Rental ID: "
                            + rentalId
                            + " và Discount ID: "
                            + discountId));
    return rentalDiscountsMapper.toRentalDiscountResponse(rentalDiscounts);
  }

  @Override
  public List<RentalDiscountResponse> getAllRentalDiscounts() {
    List<RentalDiscounts> rentalDiscounts = rentalDiscountsRepository.findAll();
    List<RentalDiscountResponse> rentalDiscountResponses = new ArrayList<>();
    for (RentalDiscounts rentalDiscount : rentalDiscounts) {
      rentalDiscountResponses.add(rentalDiscountsMapper.toRentalDiscountResponse(rentalDiscount));
    }
    return rentalDiscountResponses;
  }

  @Override
  public List<RentalDiscountResponse> getRentalDiscountsByRentalId(Long rentalId) {
    List<RentalDiscounts> rentalDiscounts =
        rentalDiscountsRepository.findByRental_RentalId(rentalId);
    List<RentalDiscountResponse> rentalDiscountResponses = new ArrayList<>();
    for (RentalDiscounts rentalDiscount : rentalDiscounts) {
      rentalDiscountResponses.add(rentalDiscountsMapper.toRentalDiscountResponse(rentalDiscount));
    }
    return rentalDiscountResponses;
  }

  @Override
  public List<RentalDiscountResponse> getRentalDiscountsByDiscountId(Long discountId) {
    List<RentalDiscounts> rentalDiscounts =
        rentalDiscountsRepository.findByDiscount_DiscountId(discountId);
    List<RentalDiscountResponse> rentalDiscountResponses = new ArrayList<>();
    for (RentalDiscounts rentalDiscount : rentalDiscounts) {
      rentalDiscountResponses.add(rentalDiscountsMapper.toRentalDiscountResponse(rentalDiscount));
    }
    return rentalDiscountResponses;
  }

  @Override
  public RentalDiscountResponse updateRentalDiscount(RentalDiscountRequest request) {
    RentalDiscountsId id = new RentalDiscountsId(request.getRentalId(), request.getDiscountId());
    RentalDiscounts existingRentalDiscount =
        rentalDiscountsRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy RentalDiscount với Rental ID: "
                            + request.getRentalId()
                            + " và Discount ID: "
                            + request.getDiscountId()));

    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy Rental với ID: " + request.getRentalId()));
    Discount discount =
        discountRepository
            .findById(request.getDiscountId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Không tìm thấy Discount với ID: " + request.getDiscountId()));

    rentalDiscountsMapper.updateRentalDiscountsFromRequest(request, existingRentalDiscount);
    existingRentalDiscount.setRental(rental);
    existingRentalDiscount.setDiscount(discount);

    return rentalDiscountsMapper.toRentalDiscountResponse(
        rentalDiscountsRepository.save(existingRentalDiscount));
  }

  @Override
  public void deleteRentalDiscount(Long rentalId, Long discountId) {
    RentalDiscountsId id = new RentalDiscountsId(rentalId, discountId);
    if (!rentalDiscountsRepository.existsById(id)) {
      throw new NotFoundException(
          "Không tìm thấy RentalDiscount với Rental ID: "
              + rentalId
              + " và Discount ID: "
              + discountId);
    }
    rentalDiscountsRepository.deleteById(id);
  }
}

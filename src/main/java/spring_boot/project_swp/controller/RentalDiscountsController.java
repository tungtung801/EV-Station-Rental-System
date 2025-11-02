package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;
import spring_boot.project_swp.service.RentalDiscountsService;

@RestController
@RequestMapping("/api/rental-discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Rental Discounts APIs", description = "APIs for managing discounts applied to rentals")
public class RentalDiscountsController {

  final RentalDiscountsService rentalDiscountsService;

  @PostMapping
  @Operation(
      summary = "Create a new rental discount",
      description = "Applies a discount to a specific rental.")
  public ResponseEntity<RentalDiscountResponse> createRentalDiscount(
      @RequestBody @Valid RentalDiscountRequest request) {
    return new ResponseEntity<>(
        rentalDiscountsService.createRentalDiscount(request), HttpStatus.CREATED);
  }

  @GetMapping("/{rentalId}/{discountId}")
  @Operation(
      summary = "Get rental discount by IDs",
      description = "Retrieves a specific rental discount by rental ID and discount ID.")
  public ResponseEntity<RentalDiscountResponse> getRentalDiscountById(
      @PathVariable Long rentalId, @PathVariable Long discountId) {
    return new ResponseEntity<>(
        rentalDiscountsService.getRentalDiscountById(rentalId, discountId), HttpStatus.OK);
  }

  @GetMapping
  @Operation(
      summary = "Get all rental discounts",
      description = "Retrieves a list of all rental discounts.")
  public ResponseEntity<List<RentalDiscountResponse>> getAllRentalDiscounts() {
    return new ResponseEntity<>(rentalDiscountsService.getAllRentalDiscounts(), HttpStatus.OK);
  }

  @GetMapping("/rental/{rentalId}")
  @Operation(
      summary = "Get rental discounts by rental ID",
      description = "Retrieves a list of rental discounts for a specific rental.")
  public ResponseEntity<List<RentalDiscountResponse>> getRentalDiscountsByRentalId(
      @PathVariable Long rentalId) {
    return new ResponseEntity<>(
        rentalDiscountsService.getRentalDiscountsByRentalId(rentalId), HttpStatus.OK);
  }

  @GetMapping("/discount/{discountId}")
  @Operation(
      summary = "Get rental discounts by discount ID",
      description = "Retrieves a list of rental discounts associated with a specific discount.")
  public ResponseEntity<List<RentalDiscountResponse>> getRentalDiscountsByDiscountId(
      @PathVariable Long discountId) {
    return new ResponseEntity<>(
        rentalDiscountsService.getRentalDiscountsByDiscountId(discountId), HttpStatus.OK);
  }

  @DeleteMapping("/{rentalId}/{discountId}")
  @Operation(
      summary = "Delete a rental discount",
      description = "Removes a discount from a rental.")
  public ResponseEntity<Void> deleteRentalDiscount(
      @PathVariable Long rentalId, @PathVariable Long discountId) {
    rentalDiscountsService.deleteRentalDiscount(rentalId, discountId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

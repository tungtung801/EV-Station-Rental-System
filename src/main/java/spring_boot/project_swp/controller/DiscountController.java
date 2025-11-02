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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;
import spring_boot.project_swp.service.DiscountService;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Discount APIs", description = "APIs for managing discounts")
public class DiscountController {

  final DiscountService discountService;

  @PostMapping
  @Operation(
      summary = "Create a new discount",
      description = "Creates a new discount with the provided details.")
  public ResponseEntity<DiscountResponse> createDiscount(
      @RequestBody @Valid DiscountRequest request) {
    return new ResponseEntity<>(discountService.createDiscount(request), HttpStatus.CREATED);
  }

  @GetMapping("/{discountId}")
  @Operation(summary = "Get discount by ID", description = "Retrieves a discount by its unique ID.")
  public ResponseEntity<DiscountResponse> getDiscountById(@PathVariable Long discountId) {
    return new ResponseEntity<>(discountService.getDiscountById(discountId), HttpStatus.OK);
  }

  @GetMapping
  @Operation(summary = "Get all discounts", description = "Retrieves a list of all discounts.")
  public ResponseEntity<List<DiscountResponse>> getAllDiscounts() {
    return new ResponseEntity<>(discountService.getAllDiscounts(), HttpStatus.OK);
  }

  @PutMapping("/{discountId}")
  @Operation(
      summary = "Update an existing discount",
      description = "Updates an existing discount with the provided details.")
  public ResponseEntity<DiscountResponse> updateDiscount(
      @PathVariable Long discountId, @RequestBody @Valid DiscountRequest request) {
    return new ResponseEntity<>(discountService.updateDiscount(discountId, request), HttpStatus.OK);
  }

  @DeleteMapping("/{discountId}")
  @Operation(summary = "Delete a discount", description = "Deletes a discount by its ID.")
  public ResponseEntity<Void> deleteDiscount(@PathVariable Long discountId) {
    discountService.deleteDiscount(discountId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/code/{code}")
  @Operation(
      summary = "Get discount by code",
      description = "Retrieves a discount by its unique code.")
  public ResponseEntity<DiscountResponse> getDiscountByCode(@PathVariable String code) {
    return new ResponseEntity<>(discountService.getDiscountByCode(code), HttpStatus.OK);
  }
}

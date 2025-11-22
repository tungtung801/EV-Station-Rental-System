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
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;
import spring_boot.project_swp.service.DiscountService;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Discount Management", description = "Admin APIs for Coupons")
public class DiscountController {

    DiscountService discountService;

    @PostMapping
    @Operation(summary = "Create new discount (Admin)")
    public ResponseEntity<DiscountResponse> createDiscount(@Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.createDiscount(request));
    }

    @GetMapping
    @Operation(summary = "Get all discounts")
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID")
    public ResponseEntity<DiscountResponse> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get discount by Code (Check valid)")
    public ResponseEntity<DiscountResponse> getDiscountByCode(@PathVariable String code) {
        return ResponseEntity.ok(discountService.getDiscountByCode(code));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update discount (Admin)")
    public ResponseEntity<DiscountResponse> updateDiscount(
            @PathVariable Long id, @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete/Deactivate discount (Admin)")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}
package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.DiscountRequest;
import spring_boot.project_swp.dto.response.DiscountResponse;
import spring_boot.project_swp.service.DiscountService;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountController {

    final DiscountService discountService;

    @PostMapping
    public ResponseEntity<DiscountResponse> createDiscount(@RequestBody @Valid DiscountRequest request) {
        return new ResponseEntity<>(discountService.createDiscount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{discountId}")
    public ResponseEntity<DiscountResponse> getDiscountById(@PathVariable Integer discountId) {
        return new ResponseEntity<>(discountService.getDiscountById(discountId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts() {
        return new ResponseEntity<>(discountService.getAllDiscounts(), HttpStatus.OK);
    }

    @PutMapping("/{discountId}")
    public ResponseEntity<DiscountResponse> updateDiscount(@PathVariable Integer discountId, @RequestBody @Valid DiscountRequest request) {
        return new ResponseEntity<>(discountService.updateDiscount(discountId, request), HttpStatus.OK);
    }

    @DeleteMapping("/{discountId}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Integer discountId) {
        discountService.deleteDiscount(discountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DiscountResponse> getDiscountByCode(@PathVariable String code) {
        return new ResponseEntity<>(discountService.getDiscountByCode(code), HttpStatus.OK);
    }
}
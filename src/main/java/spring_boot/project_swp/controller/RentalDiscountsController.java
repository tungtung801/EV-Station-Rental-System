package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RentalDiscountRequest;
import spring_boot.project_swp.dto.response.RentalDiscountResponse;
import spring_boot.project_swp.service.RentalDiscountsService;

import java.util.List;

@RestController
@RequestMapping("/api/rental-discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalDiscountsController {

    final RentalDiscountsService rentalDiscountsService;

    @PostMapping
    public ResponseEntity<RentalDiscountResponse> createRentalDiscount(@RequestBody @Valid RentalDiscountRequest request) {
        return new ResponseEntity<>(rentalDiscountsService.createRentalDiscount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{rentalId}/{discountId}")
    public ResponseEntity<RentalDiscountResponse> getRentalDiscountById(@PathVariable Integer rentalId, @PathVariable Integer discountId) {
        return new ResponseEntity<>(rentalDiscountsService.getRentalDiscountById(rentalId, discountId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<RentalDiscountResponse>> getAllRentalDiscounts() {
        return new ResponseEntity<>(rentalDiscountsService.getAllRentalDiscounts(), HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<RentalDiscountResponse>> getRentalDiscountsByRentalId(@PathVariable Integer rentalId) {
        return new ResponseEntity<>(rentalDiscountsService.getRentalDiscountsByRentalId(rentalId), HttpStatus.OK);
    }

    @GetMapping("/discount/{discountId}")
    public ResponseEntity<List<RentalDiscountResponse>> getRentalDiscountsByDiscountId(@PathVariable Integer discountId) {
        return new ResponseEntity<>(rentalDiscountsService.getRentalDiscountsByDiscountId(discountId), HttpStatus.OK);
    }

    @DeleteMapping("/{rentalId}/{discountId}")
    public ResponseEntity<Void> deleteRentalDiscount(@PathVariable Integer rentalId, @PathVariable Integer discountId) {
        rentalDiscountsService.deleteRentalDiscount(rentalId, discountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {MapperUtils.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  @Mapping(target = "paymentId", ignore = true)
  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "processedByStaff", source = "staffId")
  @Mapping(target = "transactionTime", ignore = true)
  @Mapping(target = "transactionCode", ignore = true)
  @Mapping(target = "status", ignore = true)
  Payment toPayment(PaymentRequest paymentRequest);

  @Mapping(source = "rental.rentalId", target = "rentalId")
  PaymentResponse toPaymentResponse(Payment payment);

  // Add method to convert PaymentResponse to Payment entity
  @Mapping(target = "rental", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "processedByStaff", ignore = true)
  Payment toPayment(PaymentResponse paymentResponse);
}

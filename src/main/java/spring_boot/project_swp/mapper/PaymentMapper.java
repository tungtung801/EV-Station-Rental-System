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
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "confirmedAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "booking", source = "bookingId")
  @Mapping(target = "rental", source = "rentalId")
  @Mapping(target = "confirmedBy", source = "confirmedById")
  @Mapping(target = "payer", source = "payerId")
  Payment toPayment(PaymentRequest request);

  @Mapping(target = "bookingId", source = "booking.bookingId")
  @Mapping(target = "rentalId", source = "rental.rentalId")
  @Mapping(target = "confirmedById", source = "confirmedBy.userId")
  @Mapping(target = "payerId", source = "payer.userId")
  PaymentResponse toPaymentResponse(Payment payment);
}

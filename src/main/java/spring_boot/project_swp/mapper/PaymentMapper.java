package spring_boot.project_swp.mapper;

import java.util.List; // Nhớ thêm List
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  // IGNORE HẾT CÁC TRƯỜNG QUAN HỆ (Set trong Service)
  @Mapping(target = "paymentId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "confirmedAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "booking", ignore = true)
  @Mapping(target = "rental", ignore = true)
  @Mapping(target = "payer", ignore = true)
  @Mapping(target = "confirmedBy", ignore = true)
  Payment toPayment(PaymentRequest request);

  // Map sang Response thì OK (lấy ID từ Entity ra)
  @Mapping(target = "bookingId", source = "booking.bookingId")
  @Mapping(target = "rentalId", source = "rental.rentalId")
  @Mapping(target = "confirmedById", source = "confirmedBy.userId")
  @Mapping(target = "payerId", source = "payer.userId")
  PaymentResponse toPaymentResponse(Payment payment);

  List<PaymentResponse> toPaymentResponseList(List<Payment> payments);
}

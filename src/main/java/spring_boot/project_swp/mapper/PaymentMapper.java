package spring_boot.project_swp.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RentalMapper.class, UserMapper.class})
public interface PaymentMapper {
    @Mapping(source = "rentalId", target = "rental.rentalId")
    @Mapping(source = "userId", target = "user.userId")
    @Mapping(target = "paymentId", ignore = true)
    Payment toPayment(PaymentRequest paymentRequest);

    @Mapping(source = "rentalId", target = "rental.rentalId")
    @Mapping(source = "userId", target = "user.userId")
    @Mapping(target = "paymentId", ignore = true)
    PaymentResponse toPaymentResponse(Payment payment);


}

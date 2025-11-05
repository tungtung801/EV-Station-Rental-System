package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByTransactionCode(String transactionCode);

  List<Payment> findAllByRentalRentalId(Long rentalId);

  List<Payment> findByStatus(PaymentStatusEnum status);

  Optional<Payment> findByBooking_BookingIdAndPaymentType(
      Long bookingId, PaymentTypeEnum paymentType);
}

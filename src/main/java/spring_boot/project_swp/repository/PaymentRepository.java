package spring_boot.project_swp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentStatusEnum;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByTransactionCode(String transactionCode);

  List<Payment> findAllByRental_RentalId(Long rentalId);

  Optional<Payment> findPaymentByPaymentId(Long paymentId);

  List<Payment> findByStatus(PaymentStatusEnum status);

  Payment findPaymentByTransactionCode(String transactionCode);
}

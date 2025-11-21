package spring_boot.project_swp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import spring_boot.project_swp.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByBooking_BookingId(Long bookingId);
}

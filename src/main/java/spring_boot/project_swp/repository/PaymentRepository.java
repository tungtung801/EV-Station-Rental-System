package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.User;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findPaymentByStaffId(User staffId);

    Payment findPaymentByPaymentId(int paymentId);

    List<Payment> findPaymentByStatus(String status);

    Payment findPaymentByRental_RentalId(int rentalId);

}

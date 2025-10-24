package spring_boot.project_swp.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.service.PaymentService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    @Override
    public List<PaymentResponse> findAllPaymentsStatus() {
        return List.of();
    }

    @Override
    public List<PaymentResponse> findAllPaymentsMethod(String status) {
        return List.of();
    }

    @Override
    public PaymentResponse findPaymentById(String paymentId) {
        return null;
    }

    @Override
    public PaymentResponse createPayment(Payment payment) {
        return null;
    }

    @Override
    public PaymentResponse updatePaymentStatus(String paymentId, String status) {
        return null;
    }

    @Override
    public PaymentResponse cancelPayment(String paymentId) {
        return null;
    }

    @Override
    public PaymentResponse confirmPayment(String paymentId) {
        return null;
    }

    @Override
    public PaymentResponse findPaymentByTransactionCode(String paymentId) {
        return null;
    }
}

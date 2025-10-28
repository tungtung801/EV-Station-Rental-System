package spring_boot.project_swp.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.PaymentService;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    final PaymentRepository paymentRepository;
    final PaymentMapper paymentMapper;
    final UserRepository userRepository;

    @Override
    public List<PaymentResponse> findAllPaymentsStatus() {
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentResponse> paymentResponseList = new ArrayList<>();
        for (Payment payment : payments) {
            paymentResponseList.add(paymentMapper.toPaymentResponse(payment));
        }
        return paymentResponseList;
    }

    @Override
    public List<PaymentResponse> findAllPaymentsMethod(String status) {
        List<Payment> payments = paymentRepository.findPaymentByStatus(status);
        List<PaymentResponse> paymentResponseList = new ArrayList<>();
        for (Payment payment : payments) {
            paymentResponseList.add(paymentMapper.toPaymentResponse(payment));
        }
        return paymentResponseList;
    }

    @Override
    public Payment findPaymentById(int paymentId) {
        Payment payment = paymentRepository.findPaymentByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return payment;
    }

    @Override
    public PaymentResponse createPayment(Payment payment) {

        if (payment != null) {
            if (payment.getStaffId() != null && payment.getStaffId().getUserId() != 0) {
                payment.setStaffId(userRepository.findById(payment.getStaffId().getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found")));
            }
            //Phần này của discount tạm thời chưa làm


            //
            Rental rental = payment.getRental();
            if (rental == null || rental.getRentalId() == 0) {
                throw new RuntimeException("Rental information is missing");
            }else{
                payment.setRental(rental);
                payment.setAmount(rental.getTotalCost());
            }

            //Payment method validation
            if (payment.getPaymentMethod().equalsIgnoreCase("vnpay")) {
                payment.setPaymentMethod("VNPay");
            }else if (payment.getPaymentMethod().equalsIgnoreCase("cash")) {
                payment.setPaymentMethod("Cash");
            }else{
                throw new RuntimeException("Invalid payment method");
            }

            if (payment.getStatus().equalsIgnoreCase("PENDING")) {
                payment.setPaymentMethod("PENDING");
            }

        }else{
            throw new RuntimeException("Payment information is missing");
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);

    }

    @Override
    public Payment updatePaymentStatus(int paymentId, String status) {
        Payment payment = findPaymentById(paymentId);
        if(payment != null){
            if(!status.isEmpty()){
                payment.setStatus(status);
            }else{
                throw new RuntimeException("Status is empty");
            }
        }else{
            throw new RuntimeException("Payment not found");
        }
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    public Payment updatePaymentAfterPaid(Payment payment) {
        if(payment != null){
            if(payment.getPaymentMethod().equalsIgnoreCase("deposited")){
                payment.setPaymentMethod("deposited");
                payment.setAmount(payment.getAmount());
            }

            if(payment.getTransactionCode() != null){
                payment.setTransactionCode(payment.getTransactionCode());
            }else{
                throw new RuntimeException("Payment Transaction information is missing");
            }
            if(payment.getTransactionTime()!= null){
                payment.setTransactionTime(payment.getTransactionTime());
            } else {
                throw new RuntimeException("Payment Transaction Time information is missing");
            }
        }else{
            throw new RuntimeException("Payment not found");
        }
        return paymentRepository.save(payment);
    }

    @Override
    public Payment UpdatePayment(Payment paymentUpdate) {
        Payment payment = paymentRepository.findPaymentByPaymentId(paymentUpdate.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return payment;
    }

    @Override
    public Payment cancelPayment(int paymentId) {
        return updatePaymentStatus(paymentId, "Cancelled");
    }

    @Override
    public Payment confirmPayment(int paymentId) {
        return updatePaymentStatus(paymentId, "Confirmed");
    }

    @Override
    public Payment findPaymentByTransactionCode(String transactionCode) {
        Payment payment =paymentRepository.findPaymentByTransactionCode(transactionCode);
        if(payment != null){
            return payment;
        }else{
            throw new RuntimeException("Payment not found");
        }
    }
}

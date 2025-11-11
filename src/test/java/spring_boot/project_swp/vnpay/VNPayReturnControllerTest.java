package spring_boot.project_swp.vnpay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.UserService;

@ExtendWith(MockitoExtension.class)
public class VNPayReturnControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private BookingService bookingService;
    @Mock private UserService userService;
    @Mock private RentalService rentalService; // Mock cho logic đã sửa
    @Mock private BookingMapper bookingMapper; // Thêm mapper vì controller sử dụng

    @InjectMocks private VNPayReturnController vnPayReturnController;

    private Map<String, String> vnpParams;
    private PaymentResponse paymentResponse;
    private UserResponse userResponse;
    private BookingResponse bookingResponse;
    private RentalResponse rentalResponse;

    @BeforeEach
    void setUp() {
        // Tiêm giá trị @Value bằng tay
        ReflectionTestUtils.setField(vnPayReturnController, "hashSecret", "MY_SECRET_KEY");

        // Dữ liệu mock
        paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(1L);
        paymentResponse.setBookingId(101L);
        paymentResponse.setPayerId(1L);
        paymentResponse.setTransactionCode("TXN123456");

        userResponse = new UserResponse();
        userResponse.setEmail("user@example.com");

        bookingResponse = BookingResponse.builder().bookingId(101L).build();
        rentalResponse = RentalResponse.builder().rentalId(999L).bookingId(101L).build();

        // Dữ liệu mẫu cho một giao dịch thành công
        vnpParams = new HashMap<>();
        vnpParams.put("vnp_Amount", "1000000");
        vnpParams.put("vnp_BankCode", "NCB");
        vnpParams.put("vnp_ResponseCode", "00"); // Thành công
        vnpParams.put("vnp_TxnRef", "TXN123456");
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang");
        vnpParams.put("vnp_SecureHash", "valid_hash"); // Giả sử đây là hash đúng
    }

    @Test
    public void testHandleVNPayReturn_Success_FixedLogic() {
        // --- Arrange ---
        // Giả lập (mock) phương thức static VnpayUtils.hmacSHA512
        try (MockedStatic<VnpayUtils> mockedUtils = Mockito.mockStatic(VnpayUtils.class)) {
            // Giả lập rằng hash được tính toán khớp với hash đầu vào
            mockedUtils
                    .when(() -> VnpayUtils.hmacSHA512(eq("MY_SECRET_KEY"), anyString()))
                    .thenReturn("valid_hash");

            // Giả lập các lời gọi service
            when(paymentService.findPaymentByTransactionCode("TXN123456")).thenReturn(paymentResponse);
            when(bookingService.getBookingById(101L)).thenReturn(bookingResponse);
            when(bookingMapper.toBooking(bookingResponse)).thenReturn(null); // not used downstream

            // 2. Cập nhật trạng thái payment thành SUCCESS
            // Lưu ý: paymentService.updatePaymentStatus() sẽ tự động xử lý... (giữ nguyên comment)
            when(paymentService.updatePaymentStatus(1L, PaymentStatusEnum.SUCCESS)).thenReturn(null);

            // BookingService.updateBookingStatus với method 2 tham số (Long, BookingStatusEnum)
            when(bookingService.updateBookingStatus(101L, BookingStatusEnum.DEPOSIT_PAID))
                    .thenReturn(bookingResponse);

            // 3. Lấy rental ID từ booking
            when(rentalService.createRentalFromBooking(101L)).thenReturn(null);
            when(rentalService.getAllRentals()).thenReturn(java.util.List.of(rentalResponse));

            // --- Act ---
            ResponseEntity<String> response = vnPayReturnController.handleVNPayReturn(vnpParams);

            // --- Assert ---
            // 1. Kiểm tra kết quả
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("Giao dịch thành công!"));
            assertTrue(response.getBody().contains("Rental ID: "));
            assertTrue(response.getBody().contains("transactionCode: TXN123456"));

            // 2. Xác minh (verify) rằng các service đã được gọi ĐÚNG THỨ TỰ
            verify(paymentService).findPaymentByTransactionCode("TXN123456");
            verify(bookingService).updateBookingStatus(101L, BookingStatusEnum.DEPOSIT_PAID);
            verify(paymentService).updatePaymentStatus(1L, PaymentStatusEnum.SUCCESS);

            // 3. * KIỂM TRA QUAN TRỌNG NHẤT *
            // Xác minh rằng hàm createRentalFromBooking ĐÃ ĐƯỢC GỌI
            verify(rentalService, times(1)).createRentalFromBooking(101L);
            verify(rentalService, times(1)).getAllRentals();
        }
    }

    @Test
    public void testHandleVNPayReturn_InvalidSignature() {
        // --- Arrange ---
        vnpParams.put("vnp_SecureHash", "wrong_hash");

        try (MockedStatic<VnpayUtils> mockedUtils = Mockito.mockStatic(VnpayUtils.class)) {
            // Giả lập hash tính toán (actual_hash) không khớp với hash đầu vào (wrong_hash)
            mockedUtils
                    .when(() -> VnpayUtils.hmacSHA512(eq("MY_SECRET_KEY"), anyString()))
                    .thenReturn("actual_hash");

            // --- Act ---
            ResponseEntity<String> response = vnPayReturnController.handleVNPayReturn(vnpParams);

            // --- Assert ---
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Chữ ký không hợp lệ!", response.getBody());

            // Đảm bảo không có service nào được gọi nếu chữ ký sai
            verify(paymentService, never()).findPaymentByTransactionCode(anyString());
            verify(bookingService, never()).updateBookingStatus(anyLong(), any());
            verify(rentalService, never()).createRentalFromBooking(anyLong());
        }
    }

    @Test
    public void testHandleVNPayReturn_TransactionFailed() {
        // --- Arrange ---
        vnpParams.put("vnp_ResponseCode", "01"); // Giao dịch thất bại
        vnpParams.put("vnp_SecureHash", "failed_hash");

        try (MockedStatic<VnpayUtils> mockedUtils = Mockito.mockStatic(VnpayUtils.class)) {
            // Giả lập hash khớp
            mockedUtils
                    .when(() -> VnpayUtils.hmacSHA512(eq("MY_SECRET_KEY"), anyString()))
                    .thenReturn("failed_hash");

            // --- Act ---
            ResponseEntity<String> response = vnPayReturnController.handleVNPayReturn(vnpParams);

            // --- Assert ---
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Giao dịch thất bại. Mã lỗi: 01"));

            // Đảm bảo không có service (cập nhật trạng thái) nào được gọi
            verify(paymentService, never()).findPaymentByTransactionCode(anyString());
            verify(bookingService, never()).updateBookingStatus(anyLong(), any());
            verify(rentalService, never()).createRentalFromBooking(anyLong());
        }
    }
}
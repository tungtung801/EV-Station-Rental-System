package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.RoleService;
import spring_boot.project_swp.service.UserService;
import spring_boot.project_swp.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleService roleService;
    @Mock
    private BookingRepository bookingRepository; // Dependency
    @Mock
    private RentalService rentalService; // Dependency
    @Mock
    private BookingMapper bookingMapper; // Dependency

    @InjectMocks
    private PaymentServiceImpl paymentService;

    // Biến cho Test Create Final Payment
    private Rental rental;
    private User renter;
    private User admin;
    private Role userRole;
    private Role adminRole;
    private UserResponse renterResponse;
    private UserResponse adminResponse;
    private PaymentRequest finalPaymentRequest;

    @BeforeEach
    void setUp() {
        // --- Setup cho Create Final Payment (Test 10) ---
        rental = Rental.builder()
                .rentalId(1L)
                .total(new BigDecimal("500000")) // Tổng tiền cuối cùng là 500k
                .build();

        userRole = Role.builder().roleId(1L).roleName("user").build();
        adminRole = Role.builder().roleId(2L).roleName("admin").build();

        renterResponse = new UserResponse();
        renterResponse.setUserId(1L);
        renterResponse.setEmail("renter@example.com");
        renterResponse.setRoleName("user");

        renter = User.builder().userId(1L).build();

        adminResponse = new UserResponse();
        adminResponse.setUserId(99L);
        adminResponse.setEmail("admin@gmail.com");
        adminResponse.setRoleName("admin");

        admin = User.builder().userId(99L).build();

        finalPaymentRequest = PaymentRequest.builder()
                .paymentMethod(PaymentMethodEnum.CASH)
                .confirmedById(null) // Giả sử admin xử lý (không rõ staff)
                .build();
    }

    // --- Test 10: Thanh toán 100% ---
    @Test
    public void testCreateFinalPayment_Success() {
        // Arrange
        Long rentalId = 1L;
        String userEmail = "renter@example.com";

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userService.getUserByEmail(userEmail)).thenReturn(renterResponse);
        when(userMapper.toEntity(renterResponse)).thenReturn(renter);
        when(roleService.getRoleByName("user")).thenReturn(userRole);

        // Logic của getProcessedByStaff (khi confirmedById là null, sẽ lấy admin)
        when(userService.getUserByEmail("admin@gmail.com")).thenReturn(adminResponse);
        when(userMapper.toEntity(adminResponse)).thenReturn(admin);
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(null); // Không cần

        // Act
        paymentService.createFinalPayment(rentalId, userEmail, finalPaymentRequest);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(rental, savedPayment.getRental()); // Đúng rental
        assertEquals(renter, savedPayment.getPayer()); // Đúng người trả
        assertEquals(admin, savedPayment.getConfirmedBy()); // Đúng người xử lý (admin)
        assertEquals(PaymentTypeEnum.FINAL, savedPayment.getPaymentType()); // Đúng loại
        assertEquals(new BigDecimal("500000"), savedPayment.getAmount()); // Đúng số tiền
        assertEquals(PaymentStatusEnum.PENDING, savedPayment.getStatus()); // Đúng trạng thái
    }
}
package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.Print_Exception.UserNotVerifiedException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.impl.BookingServiceImpl;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.service.impl.RentalServiceImpl;
import spring_boot.project_swp.repository.StationRepository;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RentalServiceImpl rentalServiceImpl; // Mock impl để test confirmDeposit
    @Mock
    private StationRepository stationRepository; // Dependency của BookingServiceImpl

    @InjectMocks
    private BookingServiceImpl bookingService;

    // Biến cho Test Create Booking
    private User user;
    private Vehicle vehicle;
    private UserProfile userProfile;
    private BookingRequest bookingRequest;
    private Booking booking;
    private BookingResponse bookingResponse;

    // Biến cho Test Confirm Deposit
    private Booking bookingPending;
    private Payment payment;
    private BookingResponse bookingResponseConfirmed;

    @BeforeEach
    void setUp() {
        // --- Setup cho Create Booking (Test 6) ---
        user = User.builder().userId(1L).email("test@example.com").build();
        vehicle = Vehicle.builder()
                .vehicleId(1L)
                .pricePerHour(new BigDecimal("20000"))
                .pricePerDay(new BigDecimal("160000")) // 8 * 20000
                .build();

        userProfile = UserProfile.builder()
                .profileId(1L)
                .user(user)
                .status(UserProfileStatusEnum.VERIFIED) // User đã xác thực
                .idCardUrl("some-id-card-url.jpg") // Có giấy tờ
                .build();

        bookingRequest = BookingRequest.builder()
                .vehicleId(1L)
                .bookingType(BookingTypeEnum.ONLINE)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3)) // 2 giờ
                .build();

        booking = Booking.builder()
                .bookingId(1L)
                .user(user)
                .vehicle(vehicle)
                .expectedTotal(new BigDecimal("40000")) // 2 giờ * 20000
                .depositPercent(new BigDecimal("0.1"))
                .build();

        bookingResponse = BookingResponse.builder().bookingId(1L).build();

        // --- Setup cho Confirm Deposit (Test 7 & 8) ---
        bookingPending = Booking.builder()
                .bookingId(2L)
                .status(BookingStatusEnum.PENDING_DEPOSIT) // Trạng thái chờ
                .bookingType(BookingTypeEnum.ONLINE) // Online
                .build();

        payment = Payment.builder().paymentId(1L).build();

        bookingResponseConfirmed = BookingResponse.builder()
                .bookingId(2L)
                .status(BookingStatusEnum.DEPOSIT_PAID)
                .build();
    }

    // --- Test 6: Book Xe ---
    @Test
    public void testCreateBooking_Success_Online() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId())).thenReturn(Optional.of(userProfile));
        when(vehicleRepository.findById(vehicle.getVehicleId())).thenReturn(Optional.of(vehicle));
        when(bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of()); // Không có booking nào trùng

        when(bookingMapper.toBooking(bookingRequest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);
        // Giả lập paymentService.createDepositPayment được gọi
        when(paymentService.createDepositPayment(any(Booking.class), anyString(), any())).thenReturn(null);

        // Act
        BookingResponse response = bookingService.createBooking(user.getEmail(), bookingRequest);

        // Assert
        assertNotNull(response);
        assertEquals(bookingResponse.getBookingId(), response.getBookingId());
        // Kiểm tra xem createDepositPayment đã được gọi cho booking ONLINE
        verify(paymentService).createDepositPayment(any(Booking.class), eq(user.getEmail()), any());
    }

    @Test
    public void testCreateBooking_UserNotVerified() {
        // Arrange
        userProfile.setStatus(UserProfileStatusEnum.PENDING); // User chưa xác thực
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId())).thenReturn(Optional.of(userProfile));

        // Act & Assert
        assertThrows(UserNotVerifiedException.class, () -> {
            bookingService.createBooking(user.getEmail(), bookingRequest);
        });
    }

    @Test
    public void testCreateBooking_VehicleConflict() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId())).thenReturn(Optional.of(userProfile));
        when(vehicleRepository.findById(vehicle.getVehicleId())).thenReturn(Optional.of(vehicle));
        // Giả lập có 1 booking khác bị trùng
        when(bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(new Booking()));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(user.getEmail(), bookingRequest);
        });
    }

    // --- Test 7 & 8: Trả 10% (Xác nhận cọc) & Tạo Rental ---
    @Test
    public void testConfirmDepositPayment_Success_Online() {
        // Arrange
        Long bookingId = bookingPending.getBookingId();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingPending));
        // Giả lập tìm thấy payment cọc cho booking ONLINE
        when(paymentRepository.findByBooking_BookingIdAndPaymentType(bookingId, PaymentTypeEnum.DEPOSIT))
                .thenReturn(Optional.of(payment));
        // Giả lập paymentService.updatePaymentStatus được gọi
        when(paymentService.updatePaymentStatus(payment.getPaymentId(), PaymentStatusEnum.SUCCESS))
                .thenReturn(null);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingPending);
        // Giả lập rentalService.createRentalFromBooking được gọi
        when(rentalServiceImpl.createRentalFromBooking(bookingId)).thenReturn(null);
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponseConfirmed);

        // Act
        BookingResponse response = bookingService.confirmDepositPayment(bookingId, "staff@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(BookingStatusEnum.DEPOSIT_PAID, response.getStatus());
        // (Step 7) Xác minh rằng payment cọc đã được cập nhật
        verify(paymentService).updatePaymentStatus(payment.getPaymentId(), PaymentStatusEnum.SUCCESS);
        // (Step 8) Xác minh rằng rental đã được tạo
        verify(rentalServiceImpl).createRentalFromBooking(bookingId);
        // Xác minh rằng trạng thái booking đã được lưu
        verify(bookingRepository).save(argThat(b -> b.getStatus() == BookingStatusEnum.DEPOSIT_PAID));
    }

    @Test
    public void testConfirmDepositPayment_Conflict_WrongStatus() {
        // Arrange
        bookingPending.setStatus(BookingStatusEnum.DEPOSIT_PAID); // Đã thanh toán rồi
        when(bookingRepository.findById(bookingPending.getBookingId())).thenReturn(Optional.of(bookingPending));

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            bookingService.confirmDepositPayment(bookingPending.getBookingId(), "staff@example.com");
        });
        assertEquals("Booking is not in PENDING_DEPOSIT status.", ex.getMessage());
    }
}
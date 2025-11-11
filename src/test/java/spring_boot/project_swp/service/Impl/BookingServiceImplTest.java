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
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.Print_Exception.UserNotVerifiedException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.impl.BookingServiceImpl;
import spring_boot.project_swp.service.impl.RentalServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private UserRepository userRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PaymentService paymentService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RentalServiceImpl rentalServiceImpl; // Mock impl để test confirmDeposit
    @Mock private StationRepository stationRepository; // Dependency của BookingServiceImpl

    @InjectMocks private BookingServiceImpl bookingService;

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

    // Added station for vehicle & staff matching
    private Station station;
    // Added staff user for confirmDepositPayment
    private User staffUser;
    // Role for staff
    private Role staffRole;
    // Rental response expected after confirm deposit
    private RentalResponse rentalResponse;

    @BeforeEach
    void setUp() {
        // --- Setup cho Create Booking (Test 6) ---
        user = User.builder().userId(1L).email("test@example.com").build();
        vehicle =
                Vehicle.builder()
                        .vehicleId(1L)
                        .pricePerHour(new BigDecimal("20000"))
                        .pricePerDay(new BigDecimal("160000")) // 8 * 20000
                        .build();

        userProfile =
                UserProfile.builder()
                        .profileId(1L)
                        .user(user)
                        .status(UserProfileStatusEnum.VERIFIED) // User đã xác thực
                        .idCardUrl("some-id-card-url.jpg") // Có giấy tờ
                        .build();

        bookingRequest =
                BookingRequest.builder()
                        .vehicleId(1L)
                        .bookingType(BookingTypeEnum.ONLINE)
                        .startTime(LocalDateTime.now().plusHours(1))
                        .endTime(LocalDateTime.now().plusHours(3)) // 2 giờ
                        .build();

        booking =
                Booking.builder()
                        .bookingId(1L)
                        .user(user)
                        .vehicle(vehicle)
                        .expectedTotal(new BigDecimal("40000")) // 2 giờ * 20000
                        .depositPercent(new BigDecimal("0.1"))
                        .build();

        bookingResponse = BookingResponse.builder().bookingId(1L).build();

        // --- Setup cho Confirm Deposit (Test 7 & 😎 ---
        bookingPending =
                Booking.builder()
                        .bookingId(2L)
                        .status(BookingStatusEnum.PENDING_DEPOSIT) // Trạng thái chờ
                        .bookingType(BookingTypeEnum.ONLINE) // Online
                        .build();

        payment = Payment.builder().paymentId(1L).build();

        bookingResponseConfirmed =
                BookingResponse.builder().bookingId(2L).status(BookingStatusEnum.DEPOSIT_PAID).build();

        // Create station for vehicle & staff
        station = Station.builder().stationId(10L).stationName("Test Station").build();
        // Attach station to vehicle (vehicle defined earlier in existing code)
        if (vehicle != null) {
            vehicle.setStation(station);
        }
        // Prepare staff role & user used in confirmDepositPayment
        staffRole = Role.builder().roleId(3L).roleName("Staff").build();
        staffUser =
                User.builder()
                        .userId(50L)
                        .email("staff@example.com")
                        .role(staffRole)
                        .station(station)
                        .build();
        // Attach vehicle with station to bookingPending (defined in existing code setUp below)
        if (bookingPending != null) {
            bookingPending.setVehicle(vehicle);
        }
        // Prepare rental response returned by rentalServiceImpl.getAllRentals()
        rentalResponse =
                RentalResponse.builder()
                        .rentalId(99L)
                        .bookingId(bookingPending != null ? bookingPending.getBookingId() : 2L)
                        .build();
    }

    // --- Test 6: Book Xe ---
    @Test
    public void testCreateBooking_Success_Online() {
        // Arrange
        vehicle.setStation(station); // Thêm station vào vehicle

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId()))
                .thenReturn(Optional.of(userProfile));
        when(vehicleRepository.findById(vehicle.getVehicleId())).thenReturn(Optional.of(vehicle));
        when(bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of()); // Không có booking nào trùng

        when(bookingMapper.toBooking(bookingRequest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);

        // Act
        BookingResponse response = bookingService.createBooking(user.getEmail(), bookingRequest);

        // Assert
        assertNotNull(response);
        assertEquals(bookingResponse.getBookingId(), response.getBookingId());
    }

    @Test
    public void testCreateBooking_UserNotVerified() {
        // Arrange
        userProfile.setStatus(UserProfileStatusEnum.PENDING); // User chưa xác thực
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId()))
                .thenReturn(Optional.of(userProfile));

        // Act & Assert
        assertThrows(
                UserNotVerifiedException.class,
                () -> {
                    bookingService.createBooking(user.getEmail(), bookingRequest);
                });
    }

    @Test
    public void testCreateBooking_VehicleConflict() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserUserId(user.getUserId()))
                .thenReturn(Optional.of(userProfile));
        when(vehicleRepository.findById(vehicle.getVehicleId())).thenReturn(Optional.of(vehicle));
        // Giả lập có 1 booking khác bị trùng
        when(bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(new Booking()));

        // Act & Assert
        assertThrows(
                ConflictException.class,
                () -> {
                    bookingService.createBooking(user.getEmail(), bookingRequest);
                });
    }

    // --- Test 7 & 8: Trả 10% (Xác nhận cọc) & Tạo Rental ---
    @Test
    public void testConfirmDepositPayment_Success_Online() {
        // Arrange
        Long bookingId = bookingPending.getBookingId();

        // Mock staff user retrieval (required by service)
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staffUser));
        // First find booking, then again after payment update (service calls findById twice)
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(bookingPending), Optional.of(bookingPending));
        // Mock deposit payment fetch
        when(paymentRepository.findByBooking_BookingIdAndPaymentType(
                bookingId, PaymentTypeEnum.DEPOSIT))
                .thenReturn(Optional.of(payment));
        // Mock payment status update
        when(paymentService.updatePaymentStatus(payment.getPaymentId(), PaymentStatusEnum.SUCCESS))
                .thenReturn(null);
        // Mock booking save after status set to DEPOSIT_PAID
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        // Mock rental creation & retrieval list containing rental with matching bookingId
        when(rentalServiceImpl.createRentalFromBooking(bookingId)).thenReturn(null);
        when(rentalServiceImpl.getAllRentals()).thenReturn(List.of(rentalResponse));
        // Map updated booking to response (service will then set rental)
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponseConfirmed);

        // Act
        BookingResponse response = bookingService.confirmDepositPayment(bookingId, "staff@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(BookingStatusEnum.DEPOSIT_PAID, response.getStatus());
        assertNotNull(response.getRental()); // Rental should be attached
        assertEquals(rentalResponse.getRentalId(), response.getRental().getRentalId());
        // (Step 7) Xác minh rằng payment cọc đã được cập nhật
        verify(paymentService).updatePaymentStatus(payment.getPaymentId(), PaymentStatusEnum.SUCCESS);
        // (Step 😎 Xác minh rằng rental đã được tạo
        verify(rentalServiceImpl).createRentalFromBooking(bookingId);
        // Xác minh rằng trạng thái booking đã được lưu là DEPOSIT_PAID
        verify(bookingRepository, atLeastOnce())
                .save(argThat(b -> b.getStatus() == BookingStatusEnum.DEPOSIT_PAID));
        // Verify second findById call occurred (fetch updated booking)
        verify(bookingRepository, atLeast(2)).findById(bookingId);
    }

    @Test
    public void testConfirmDepositPayment_Conflict_WrongStatus() {
        // Arrange
        bookingPending.setStatus(BookingStatusEnum.DEPOSIT_PAID); // Đã thanh toán rồi
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staffUser));
        when(bookingRepository.findById(bookingPending.getBookingId()))
                .thenReturn(Optional.of(bookingPending));

        // Act & Assert
        Exception ex =
                assertThrows(
                        ConflictException.class,
                        () -> {
                            bookingService.confirmDepositPayment(
                                    bookingPending.getBookingId(), "staff@example.com");
                        });
        assertEquals("Booking is not in PENDING_DEPOSIT status.", ex.getMessage());
    }
}
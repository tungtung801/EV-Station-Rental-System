package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.mapper.RentalMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.impl.RentalServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private StationRepository stationRepository; // Dependency
    @Mock
    private BookingRepository bookingRepository; // Dependency
    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalServiceImpl rentalService;

    // Biến cho Test Confirm Return
    private Rental rental;
    private User staff;
    private Vehicle vehicle;
    private Station station;

    @BeforeEach
    void setUp() {
        // --- Setup cho Confirm Return (Test 9) ---
        station = Station.builder().stationId(1L).build();
        staff = User.builder().userId(2L).email("staff@example.com").station(station).build();
        vehicle = Vehicle.builder().vehicleId(1L).station(station).build();

        rental = Rental.builder()
                .rentalId(1L)
                .status(RentalStatusEnum.IN_PROGRESS) // Rental đang diễn ra
                .vehicle(vehicle)
                .booking(Booking.builder() // Cần mock booking để `recalculateTotal`
                        .expectedTotal(new BigDecimal("100000"))
                        .endTime(LocalDateTime.now().plusHours(1)) // Giả sử trả đúng giờ (chưa vượt quá)
                        .build())
                .build();
    }



    @Test
    public void testConfirmReturn_Conflict_StaffNotInStation() {
        // Arrange
        // Staff ở trạm 2, nhưng xe (và trạm của xe) ở trạm 1
        Station staffStation = Station.builder().stationId(2L).build();
        staff.setStation(staffStation);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            rentalService.confirmReturn(1L, "staff@example.com");
        });
        assertEquals("Nhân viên nhận xe không thuộc trạm của xe.", ex.getMessage());
    }

    // --- Test 9: Rental Hoàn Tất ---
    @Test
    public void testConfirmReturn_Success() {
        // Arrange
        String staffEmail = "staff@example.com";
        Long rentalId = 1L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(staffEmail)).thenReturn(Optional.of(staff));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Mock `recalculateTotal` (bằng cách mock lời gọi `save` bên trong nó)
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
            // Trả về chính đối tượng đã được cập nhật
            Rental savedRental = invocation.getArgument(0);
            savedRental.setTotal(new BigDecimal("100000")); // Giả lập tổng tiền không đổi
            return savedRental;
        });

        // Giả lập mapper trả về response (nếu cần)
        when(rentalMapper.toRentalResponse(any(Rental.class))).thenReturn(new RentalResponse());

        // Act
        rentalService.confirmReturn(rentalId, staffEmail);

        // Assert
        // Bắt đối tượng Vehicle được lưu
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(station, vehicleCaptor.getValue().getStation()); // Xe đã về đúng trạm

        // Bắt đối tượng Rental được lưu (bởi recalculateTotal)
        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());

        Rental savedRental = rentalCaptor.getValue();
        assertEquals(RentalStatusEnum.COMPLETED, savedRental.getStatus()); // Trạng thái
        assertEquals(staff, savedRental.getReturnStaff()); // Nhân viên
        assertEquals(station, savedRental.getReturnStation()); // Trạm
        assertNotNull(savedRental.getEndActual()); // Đã set giờ
    }

    @Test
    public void testConfirmReturn_Conflict_WrongStatus() {
        // Arrange
        rental.setStatus(RentalStatusEnum.COMPLETED); // Đã hoàn tất rồi
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            rentalService.confirmReturn(1L, "staff@example.com");
        });
        assertEquals("Rental is not in IN_PROGRESS status", ex.getMessage());
    }
}
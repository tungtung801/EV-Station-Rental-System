package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.request.RentalReturnRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.BadRequestException;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RentalMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class RentalServiceImpl implements RentalService {

    final RentalRepository rentalRepository;
    final BookingRepository bookingRepository;
    final UserRepository userRepository;
    final StationRepository stationRepository;
    final VehicleRepository vehicleRepository;
    final PaymentRepository paymentRepository;
    final VehicleCheckRepository vehicleCheckRepository;
    final RentalMapper rentalMapper;
    final FileStorageService fileStorageService; // Inject thêm cái này để up ảnh

    // 1. TẠO RENTAL (STAFF)
    @Override
    @Transactional
    public RentalResponse createRentalFromBooking(Long bookingId, Long staffId) {
        if (rentalRepository.existsByBookingBookingId(bookingId)) {
            return rentalMapper.toRentalResponse(rentalRepository.findByBooking_BookingId(bookingId).get());
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new ConflictException("Booking must be CONFIRMED to create rental");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        return createRentalInternal(booking, staff);
    }

    // 1A. TẠO RENTAL (AUTO)
    @Override
    @Transactional
    public RentalResponse createRentalFromBookingAuto(Long bookingId) {
        if (rentalRepository.existsByBookingBookingId(bookingId)) {
            return rentalMapper.toRentalResponse(rentalRepository.findByBooking_BookingId(bookingId).get());
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        return createRentalInternal(booking, null);
    }

    // Helper tạo rental chung
    private RentalResponse createRentalInternal(Booking booking, User staff) {
        if (booking.getVehicle().getVehicleStatus() != VehicleStatusEnum.AVAILABLE) {
            throw new ConflictException("Vehicle is not AVAILABLE");
        }

        Rental rental = Rental.builder()
                .booking(booking)
                .renter(booking.getUser())
                .vehicle(booking.getVehicle())
                .pickupStation(booking.getVehicle().getStation())
                .pickupStaff(staff)
                .status(RentalStatusEnum.PENDING_PICKUP)
                .total(booking.getTotalAmount())
                .build();

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    // 2. GIAO XE (FIXED LOGIC)
    @Override
    @Transactional
    public RentalResponse confirmPickup(Long rentalId, Long staffId, RentalConfirmPickupRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (rental.getStatus() != RentalStatusEnum.PENDING_PICKUP) {
            if (rental.getStatus() == RentalStatusEnum.ACTIVE) return rentalMapper.toRentalResponse(rental);
            throw new ConflictException("Rental is not in PENDING_PICKUP state");
        }

        User staff = userRepository.findById(staffId).orElseThrow(() -> new NotFoundException("Staff not found"));

        // --- FIX 1: Xử lý upload ảnh hợp đồng ---
        if (request.getContractImage() != null && !request.getContractImage().isEmpty()) {
            String url = fileStorageService.saveFile(request.getContractImage());
            rental.setContractUrl(url);
        }

        // Cập nhật thông tin
        rental.setStartActual(LocalDateTime.now());
        rental.setPickupStaff(staff);
        rental.setPickupNote(request.getPickupNote());
        rental.setStartOdometer(request.getCurrentOdometer()); // SET ODOMETER LÚC GIAO

        // VALIDATION: Kiểm tra StartOdometer hợp lệ
        if (rental.getStartOdometer() != null && rental.getStartOdometer() < 0) {
            throw new BadRequestException("Start odometer must be greater than or equal to 0");
        }

        rental.setStatus(RentalStatusEnum.ACTIVE);

        // Update Booking -> IN_PROGRESS
        Booking booking = rental.getBooking();
        booking.setStatus(BookingStatusEnum.IN_PROGRESS);
        bookingRepository.save(booking);

        // --- FIX 2: KHÔNG SET STATUS XE LÀ RENTED ---
        // Chỉ cần Booking là IN_PROGRESS thì hàm Search đã tự loại xe này ra rồi.
        // Giữ nguyên status xe là AVAILABLE (tức là xe tốt, không hỏng).

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    // 3. TRẢ XE (RETURN) - FINAL VERSION
    @Override
    @Transactional
    public RentalResponse returnVehicle(Long rentalId, Long returnStationId, Long staffId, RentalReturnRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (rental.getStatus() != RentalStatusEnum.ACTIVE) {
            throw new ConflictException("Rental is not ACTIVE");
        }

        // VALIDATION: Kiểm tra Vehicle Check phải được tạo trước khi trả xe
        VehicleChecks vehicleCheck = vehicleCheckRepository.findById(request.getCheckId())
                .orElseThrow(() -> new NotFoundException("Vehicle check not found with ID: " + request.getCheckId()));

        if (!vehicleCheck.getRental().getRentalId().equals(rentalId)) {
            throw new BadRequestException("Vehicle check does not belong to this rental");
        }

        Station returnStation = stationRepository.findById(returnStationId)
                .orElseThrow(() -> new NotFoundException("Return station not found"));
        User staff = userRepository.findById(staffId).orElseThrow(() -> new NotFoundException("Staff not found"));

        LocalDateTime endActualTime = LocalDateTime.now();

        // 1. Cập nhật thông tin cơ bản
        rental.setEndActual(endActualTime);
        rental.setReturnStation(returnStation);
        rental.setReturnStaff(staff);
        rental.setReturnCheck(vehicleCheck);
        rental.setStatus(RentalStatusEnum.COMPLETED);

        // 2. Cập nhật thông tin từ Request (Odometer & Note)
        if (request != null) {
            rental.setEndOdometer(request.getReturnOdometer());
            rental.setReturnNote(request.getReturnNote());

            // VALIDATION: Kiểm tra EndOdometer >= StartOdometer
            if (rental.getStartOdometer() != null && rental.getEndOdometer() != null) {
                if (rental.getEndOdometer() < rental.getStartOdometer()) {
                    throw new BadRequestException(
                        "End odometer (" + rental.getEndOdometer() + ") must be greater than or equal to " +
                        "start odometer (" + rental.getStartOdometer() + ")"
                    );
                }
            }
        }

        // 3. Tính toán Tiền (Late Fee + Excess KM Fee + Surcharge)
        Booking booking = rental.getBooking();
        BigDecimal lateFee = calculateLateFee(booking.getEndTime(), endActualTime, rental.getVehicle().getPricePerHour());

        // Tính phí vượt km (350km/ngày → 3.000đ/km)
        BigDecimal excessKmFee = calculateExcessKmFee(
            rental.getStartOdometer(),
            request.getReturnOdometer(),
            booking.getStartTime(),
            booking.getEndTime()
        );

        // Tổng tiền phát sinh thêm = Late Fee + Excess KM Fee
        BigDecimal totalExtraFee = lateFee.add(excessKmFee);

        // Cập nhật Total = Total cũ + Extra Fee
        if (totalExtraFee.compareTo(BigDecimal.ZERO) > 0) {
            rental.setTotal(rental.getTotal().add(totalExtraFee));

            // Tạo Payment phạt (Chờ thu tiền)
            Payment penalty = Payment.builder()
                    .rental(rental)
                    .booking(booking)
                    .payer(rental.getRenter())
                    .amount(totalExtraFee) // Thu phí trễ + phí vượt km
                    .paymentType(PaymentTypeEnum.PENALTY)
                    .paymentMethod(PaymentMethodEnum.CASH)
                    .status(PaymentStatusEnum.PENDING)
                    .note("Late fee: " + lateFee + ", Excess KM fee: " + excessKmFee)
                    .build();
            paymentRepository.save(penalty);
        }

        rentalRepository.save(rental);

        // 4. Kết thúc Booking
        booking.setStatus(BookingStatusEnum.COMPLETED);
        bookingRepository.save(booking);

        // 5. Giải phóng xe
        Vehicle vehicle = rental.getVehicle();
        vehicle.setStation(returnStation); // Xe về trạm mới
        vehicle.setVehicleStatus(VehicleStatusEnum.AVAILABLE); // Xe rảnh
        vehicleRepository.save(vehicle);

        return rentalMapper.toRentalResponse(rental);
    }

    // ... (Hàm calculateLateFee và getById giữ nguyên như cũ) ...
    private BigDecimal calculateLateFee(LocalDateTime expectedEndTime, LocalDateTime actualEndTime, BigDecimal pricePerHour) {
        if (actualEndTime.isBefore(expectedEndTime) || actualEndTime.isEqual(expectedEndTime)) {
            return BigDecimal.ZERO;
        }
        Duration lateDuration = Duration.between(expectedEndTime, actualEndTime);
        long lateHours = lateDuration.toHours();
        if (lateDuration.toMinutes() % 60 > 0) lateHours++;

        BigDecimal hourlyLateFee = BigDecimal.valueOf(70000);
        BigDecimal totalLateFee = hourlyLateFee.multiply(BigDecimal.valueOf(lateHours));

        if (lateHours > 5) {
            totalLateFee = totalLateFee.add(pricePerHour.multiply(BigDecimal.valueOf(24)));
        }
        return totalLateFee;
    }

    // Tính phí vượt km (3.000đ/km, giới hạn 350km/ngày)
    private BigDecimal calculateExcessKmFee(Integer startOdometer, Integer endOdometer, LocalDateTime startTime, LocalDateTime endTime) {
        // Kiểm tra dữ liệu
        if (startOdometer == null || endOdometer == null) {
            return BigDecimal.ZERO;
        }

        // Tính số km thực tế
        long actualKm = endOdometer - startOdometer;
        if (actualKm < 0) {
            return BigDecimal.ZERO; // Không có km, không tính phí
        }

        // Tính số ngày (làm tròn lên)
        Duration duration = Duration.between(startTime, endTime);
        long days = duration.toDays();
        if (duration.toHours() % 24 > 0) {
            days++; // Làm tròn lên (ví dụ: 1.5 ngày = 2 ngày)
        }
        if (days == 0) days = 1; // Tối thiểu 1 ngày

        // Giới hạn km
        long maxKmAllowed = 350 * days;

        // Tính phí nếu vượt
        if (actualKm > maxKmAllowed) {
            long excessKm = actualKm - maxKmAllowed;
            return BigDecimal.valueOf(excessKm).multiply(BigDecimal.valueOf(3000));
        }

        return BigDecimal.ZERO;
    }

    @Override
    public RentalResponse getRentalById(Long rentalId) {
        return rentalMapper.toRentalResponse(rentalRepository.findById(rentalId).orElseThrow());
    }

    @Override
    public List<RentalResponse> getAllRentals() {
        return rentalMapper.toRentalResponseList(rentalRepository.findAll());
    }
}
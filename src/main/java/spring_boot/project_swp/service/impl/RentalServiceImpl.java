package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.RentalConfirmPickupRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RentalMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalServiceImpl implements RentalService {

    final RentalRepository rentalRepository;
    final BookingRepository bookingRepository;
    final UserRepository userRepository;
    final StationRepository stationRepository;
    final VehicleRepository vehicleRepository;
    final RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalResponse createRentalFromBooking(Long bookingId, Long staffId) {
        // Kiểm tra đã tồn tại chưa
        if (rentalRepository.existsByBookingBookingId(bookingId)) {
            return rentalMapper.toRentalResponse(rentalRepository.findByBooking_BookingId(bookingId).get());
        }

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatusEnum.CONFIRMED) {
            // Cho phép tạo nếu CONFIRMED (vừa thanh toán xong)
            throw new ConflictException("Booking must be CONFIRMED to create rental");
        }

        User staff = userRepository.findById(staffId).orElseThrow(() -> new NotFoundException("Staff not found"));

        Rental rental = new Rental();
        rental.setBooking(booking);
        rental.setRenter(booking.getUser());
        rental.setVehicle(booking.getVehicle());
        rental.setPickupStation(booking.getVehicle().getStation());
        rental.setPickupStaff(staff);
        rental.setStatus(RentalStatusEnum.PENDING_PICKUP); // CHỜ NHẬN XE
        rental.setTotal(booking.getTotalAmount());

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse createRentalFromBookingAuto(Long bookingId) {
        if (rentalRepository.existsByBookingBookingId(bookingId)) {
            return rentalMapper.toRentalResponse(rentalRepository.findByBooking_BookingId(bookingId).get());
        }

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));

        // Tìm Admin để gán tạm (System Auto)
        User adminStaff = userRepository.findByEmail("admin@gmail.com").orElse(null);
        // Nếu ko tìm thấy admin, để null pickupStaff cũng được (sẽ update khi khách đến nhận xe)

        Rental rental = new Rental();
        rental.setBooking(booking);
        rental.setRenter(booking.getUser());
        rental.setVehicle(booking.getVehicle());
        rental.setPickupStation(booking.getVehicle().getStation());
        rental.setPickupStaff(adminStaff);
        rental.setStatus(RentalStatusEnum.PENDING_PICKUP); // CHỜ NHẬN XE
        rental.setTotal(booking.getTotalAmount());

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    // Hàm này dùng khi Staff dùng app quét mã QR hoặc bấm nút "Giao xe" (nếu có endpoint riêng)
    @Override
    @Transactional
    public RentalResponse confirmPickup(Long rentalId, RentalConfirmPickupRequest request) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new NotFoundException("Rental not found"));

        if (rental.getStatus() != RentalStatusEnum.PENDING_PICKUP) {
            // Nếu đã Active rồi thì thôi, trả về luôn
            if (rental.getStatus() == RentalStatusEnum.ACTIVE) return rentalMapper.toRentalResponse(rental);
            throw new ConflictException("Rental is not pending pickup");
        }

        rental.setContractUrl(request.getContractUrl());
        rental.setStartActual(LocalDateTime.now());
        rental.setStatus(RentalStatusEnum.ACTIVE); // CHUYỂN SANG ĐANG CHẠY

        Booking booking = rental.getBooking();
        booking.setStatus(BookingStatusEnum.IN_PROGRESS);
        bookingRepository.save(booking);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setVehicleStatus("Rented");
        vehicleRepository.save(vehicle);

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse returnVehicle(Long rentalId, Long returnStationId, Long staffId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new NotFoundException("Rental not found"));

        if (rental.getStatus() != RentalStatusEnum.ACTIVE) {
            throw new ConflictException("Rental is not active");
        }

        Station returnStation = stationRepository.findById(returnStationId).orElseThrow(() -> new NotFoundException("Return station not found"));
        User staff = userRepository.findById(staffId).orElseThrow(() -> new NotFoundException("Staff not found"));

        rental.setEndActual(LocalDateTime.now());
        rental.setReturnStation(returnStation);
        rental.setReturnStaff(staff);
        rental.setStatus(RentalStatusEnum.COMPLETED);

        Booking booking = rental.getBooking();
        booking.setStatus(BookingStatusEnum.COMPLETED);
        bookingRepository.save(booking);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setVehicleStatus("Available");
        vehicle.setStation(returnStation);
        vehicleRepository.save(vehicle);

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    // ... Các hàm get giữ nguyên
    @Override
    public RentalResponse getRentalById(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new NotFoundException("Rental not found"));
        return rentalMapper.toRentalResponse(rental);
    }
    @Override
    public List<RentalResponse> getAllRentals() {
        List<Rental> rentals = rentalRepository.findAll();
        List<RentalResponse> responses = new ArrayList<>();
        for (Rental r : rentals) responses.add(rentalMapper.toRentalResponse(r));
        return responses;
    }
}
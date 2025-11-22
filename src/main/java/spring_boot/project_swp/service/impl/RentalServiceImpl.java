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
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Chỉ tạo Rental nếu Booking đã CONFIRMED (đã chốt/đã thanh toán)
        if (booking.getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new ConflictException("Booking must be CONFIRMED to create rental");
        }

        User staff =
                userRepository
                        .findById(staffId)
                        .orElseThrow(() -> new NotFoundException("Staff not found"));

        Rental rental = new Rental();
        rental.setBooking(booking);
        rental.setRenter(booking.getUser());
        rental.setVehicle(booking.getVehicle());
        rental.setPickupStation(booking.getVehicle().getStation()); // Mặc định lấy tại trạm của xe
        rental.setPickupStaff(staff);
        rental.setStatus(RentalStatusEnum.PENDING_PICKUP); // Chờ khách đến

        // Sao chép giá tiền từ Booking sang (Tạm thời chưa tính phí phát sinh)
        rental.setTotal(booking.getTotalAmount());

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse createRentalFromBookingAuto(Long bookingId) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Chỉ tạo Rental nếu Booking đã CONFIRMED (đã chốt/đã thanh toán)
        if (booking.getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new ConflictException("Booking must be CONFIRMED to create rental");
        }

        // Tìm Admin user để làm staff (auto-create)
        User adminStaff =
                userRepository
                        .findByEmail("admin@gmail.com")
                        .orElseThrow(() -> new NotFoundException("Admin user not found for auto rental creation"));

        Rental rental = new Rental();
        rental.setBooking(booking);
        rental.setRenter(booking.getUser());
        rental.setVehicle(booking.getVehicle());
        rental.setPickupStation(booking.getVehicle().getStation()); // Mặc định lấy tại trạm của xe
        rental.setPickupStaff(adminStaff); // Set admin as staff
        rental.setStatus(RentalStatusEnum.PENDING_PICKUP); // Chờ khách đến

        // Sao chép giá tiền từ Booking sang (Tạm thời chưa tính phí phát sinh)
        rental.setTotal(booking.getTotalAmount());

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse confirmPickup(Long rentalId, RentalConfirmPickupRequest request) {
        Rental rental =
                rentalRepository
                        .findById(rentalId)
                        .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (rental.getStatus() != RentalStatusEnum.PENDING_PICKUP) {
            throw new ConflictException("Rental is not pending pickup");
        }

        rental.setContractUrl(request.getContractUrl());
        rental.setStartActual(LocalDateTime.now());
        // --- FIX: Đổi sang ACTIVE cho đồng bộ ---
        rental.setStatus(RentalStatusEnum.ACTIVE);

        // Update Booking -> IN_PROGRESS
        Booking booking = rental.getBooking();
        booking.setStatus(BookingStatusEnum.IN_PROGRESS);
        bookingRepository.save(booking);

        // Update Vehicle -> Rented (Đang cho thuê)
        Vehicle vehicle = rental.getVehicle();
        vehicle.setVehicleStatus("Rented");
        vehicleRepository.save(vehicle);

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse returnVehicle(Long rentalId, Long returnStationId, Long staffId) {
        Rental rental =
                rentalRepository
                        .findById(rentalId)
                        .orElseThrow(() -> new NotFoundException("Rental not found"));

        // --- FIX: Kiểm tra trạng thái ACTIVE thay vì IN_PROGRESS (nếu enum chỉ có ACTIVE) ---
        if (rental.getStatus() != RentalStatusEnum.ACTIVE) {
            throw new ConflictException("Rental is not in progress");
        }

        Station returnStation =
                stationRepository
                        .findById(returnStationId)
                        .orElseThrow(() -> new NotFoundException("Return station not found"));
        User staff =
                userRepository
                        .findById(staffId)
                        .orElseThrow(() -> new NotFoundException("Staff not found"));

        rental.setEndActual(LocalDateTime.now());
        rental.setReturnStation(returnStation);
        rental.setReturnStaff(staff);
        rental.setStatus(RentalStatusEnum.COMPLETED);

        // Update Booking -> COMPLETED
        Booking booking = rental.getBooking();
        booking.setStatus(BookingStatusEnum.COMPLETED);
        bookingRepository.save(booking);

        // Update Vehicle -> Available (Và cập nhật vị trí mới)
        Vehicle vehicle = rental.getVehicle();
        vehicle.setVehicleStatus("Available");
        vehicle.setStation(returnStation); // Xe đổi trạm về nơi trả
        vehicleRepository.save(vehicle);

        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Override
    public RentalResponse getRentalById(Long rentalId) {
        Rental rental =
                rentalRepository
                        .findById(rentalId)
                        .orElseThrow(() -> new NotFoundException("Rental not found"));
        return rentalMapper.toRentalResponse(rental);
    }

    @Override
    public List<RentalResponse> getAllRentals() {
        List<Rental> rentals = rentalRepository.findAll();
        List<RentalResponse> responses = new ArrayList<>();
        for (Rental r : rentals) {
            responses.add(rentalMapper.toRentalResponse(r));
        }
        return responses;
    }
}
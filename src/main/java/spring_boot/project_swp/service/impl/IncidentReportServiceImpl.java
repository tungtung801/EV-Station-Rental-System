package spring_boot.project_swp.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.entity.IncidentReports;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.IncidentReportMapper;
import spring_boot.project_swp.repository.IncidentReportRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.IncidentReportService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IncidentReportServiceImpl implements IncidentReportService {

    final IncidentReportRepository incidentReportRepository;
    final RentalRepository rentalRepository;
    final UserRepository userRepository;
    final VehicleRepository vehicleRepository;
    final IncidentReportMapper incidentReportMapper;

    // 1. TẠO BÁO CÁO (Đã sửa logic tìm theo BookingId)
    @Override
    @Transactional
    public IncidentReportResponse createIncidentReport(IncidentReportRequest request) {
        // Tìm Rental dựa trên Booking ID (vì FE gửi BookingID vào trường rentalId)
        Rental rental = rentalRepository.findByBooking_BookingId(request.getRentalId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Hợp đồng thuê (Rental) ứng với Booking ID: " + request.getRentalId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        IncidentReports report = new IncidentReports();
        report.setRental(rental);
        report.setUser(user);
        report.setVehicle(vehicle);
        report.setDescription(request.getDescription());
        report.setImageUrls(request.getImageUrls());

        // Mặc định là PENDING
        report.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");

        return incidentReportMapper.toIncidentReportResponse(incidentReportRepository.save(report));
    }

    // 2. LẤY TẤT CẢ BÁO CÁO
    @Override
    public List<IncidentReportResponse> getAllIncidentReports() {
        return incidentReportRepository.findAll().stream()
                .map(incidentReportMapper::toIncidentReportResponse)
                .collect(Collectors.toList());
    }

    // 3. LẤY CHI TIẾT THEO ID (Hàm bị thiếu gây lỗi đỏ của bạn)
    @Override
    public IncidentReportResponse getIncidentReportById(Long reportId) {
        IncidentReports report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Incident Report not found: " + reportId));
        return incidentReportMapper.toIncidentReportResponse(report);
    }

    // 4. CẬP NHẬT BÁO CÁO (Dùng để Admin đổi trạng thái)
    @Override
    @Transactional
    public IncidentReportResponse updateIncidentReport(Long reportId, IncidentReportRequest request) {
        IncidentReports report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Incident Report not found: " + reportId));

        // Cập nhật trạng thái (QUAN TRỌNG)
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }
        // Cập nhật các thông tin khác nếu có
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        if (request.getImageUrls() != null) {
            report.setImageUrls(request.getImageUrls());
        }

        return incidentReportMapper.toIncidentReportResponse(incidentReportRepository.save(report));
    }

    // 5. XÓA BÁO CÁO
    @Override
    @Transactional
    public void deleteIncidentReport(Long reportId) {
        if (!incidentReportRepository.existsById(reportId)) {
            throw new NotFoundException("Incident Report not found: " + reportId);
        }
        incidentReportRepository.deleteById(reportId);
    }
}
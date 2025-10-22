package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.entity.IncidentReports;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.IncidentReportMapper;
import spring_boot.project_swp.repository.IncidentReportRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleCheckRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.IncidentReportService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IncidentReportServiceImpl implements IncidentReportService {

    final IncidentReportRepository incidentReportRepository;
    final IncidentReportMapper incidentReportMapper;
    final RentalRepository rentalRepository;
    final VehicleRepository vehicleRepository;
    final UserRepository userRepository;
    final VehicleCheckRepository vehicleCheckRepository;

    @Override
    public IncidentReportResponse createIncidentReport(IncidentReportRequest request) {
        if (rentalRepository.findById(request.getRentalId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy Rental với ID: " + request.getRentalId());
        }
        if (vehicleRepository.findById(request.getVehicleId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy Vehicle với ID: " + request.getVehicleId());
        }
        if (userRepository.findById(request.getUserId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy User với ID: " + request.getUserId());
        }
        if (request.getCheckId() != null && vehicleCheckRepository.findById(request.getCheckId()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy VehicleCheck với ID: " + request.getCheckId());
        }

        IncidentReports incidentReports = incidentReportMapper.toIncidentReports(request);
        return incidentReportMapper.toIncidentReportResponse(incidentReportRepository.save(incidentReports));
    }

    @Override
    public List<IncidentReportResponse> getAllIncidentReports() {
        List<IncidentReports> incidentReportsList = incidentReportRepository.findAll();
        List<IncidentReportResponse> responseList = new ArrayList<>();
        for (IncidentReports incidentReports : incidentReportsList) {
            responseList.add(incidentReportMapper.toIncidentReportResponse(incidentReports));
        }
        return responseList;
    }

    @Override
    public IncidentReportResponse getIncidentReportById(Long reportId) {
        Optional<IncidentReports> incidentReportsOptional = incidentReportRepository.findById(reportId);
        if (incidentReportsOptional.isEmpty()) {
            throw new NotFoundException("Không tìm thấy IncidentReport với ID: " + reportId);
        }
        return incidentReportMapper.toIncidentReportResponse(incidentReportsOptional.get());
    }

    @Override
    public IncidentReportResponse updateIncidentReport(Long reportId, IncidentReportRequest request) {
        Optional<IncidentReports> incidentReportsOptional = incidentReportRepository.findById(reportId);
        if (incidentReportsOptional.isEmpty()) {
            throw new NotFoundException("Không tìm thấy IncidentReport với ID: " + reportId);
        }

        if (rentalRepository.findById(request.getRentalId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy Rental với ID: " + request.getRentalId());
        }
        if (vehicleRepository.findById(request.getVehicleId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy Vehicle với ID: " + request.getVehicleId());
        }
        if (userRepository.findById(request.getUserId().intValue()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy User với ID: " + request.getUserId());
        }
        if (request.getCheckId() != null && vehicleCheckRepository.findById(request.getCheckId()).isEmpty()) {
            throw new NotFoundException("Không tìm thấy VehicleCheck với ID: " + request.getCheckId());
        }

        IncidentReports existingIncidentReport = incidentReportsOptional.get();
        incidentReportMapper.updateIncidentReports(request, existingIncidentReport);
        return incidentReportMapper.toIncidentReportResponse(incidentReportRepository.save(existingIncidentReport));
    }

    @Override
    public void deleteIncidentReport(Long reportId) {
        if (incidentReportRepository.findById(reportId).isEmpty()) {
            throw new NotFoundException("Không tìm thấy IncidentReport với ID: " + reportId);
        }
        incidentReportRepository.deleteById(reportId);
    }
}
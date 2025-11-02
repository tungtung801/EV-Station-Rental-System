package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
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

// ... các import khác giữ nguyên
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
    if (userRepository.findById(request.getUserId()).isEmpty()) {
      throw new NotFoundException("User not found");
    }
    if (rentalRepository.findById(request.getRentalId()).isEmpty()) {
      throw new NotFoundException("Rental not found with ID: " + request.getRentalId());
    }
    if (vehicleRepository.findById(request.getVehicleId().longValue()).isEmpty()) {
      throw new NotFoundException("Vehicle not found with ID: " + request.getVehicleId());
    }
    if (userRepository.findById(request.getUserId().longValue()).isEmpty()) {
      throw new NotFoundException("User not found with ID: " + request.getUserId());
    }
    if (request.getCheckId() != null
        && vehicleCheckRepository.findById(request.getCheckId()).isEmpty()) {
      throw new NotFoundException("VehicleCheck not found with ID: " + request.getCheckId());
    }

    IncidentReports incidentReports = incidentReportMapper.toIncidentReports(request);
    return incidentReportMapper.toIncidentReportResponse(
        incidentReportRepository.save(incidentReports));
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
      throw new NotFoundException("IncidentReport not found with ID: " + reportId);
    }
    return incidentReportMapper.toIncidentReportResponse(incidentReportsOptional.get());
  }

  @Override
  public IncidentReportResponse updateIncidentReport(Long reportId, IncidentReportRequest request) {
    Optional<IncidentReports> incidentReportsOptional = incidentReportRepository.findById(reportId);
    if (incidentReportsOptional.isEmpty()) {
      throw new NotFoundException("IncidentReport not found with ID: " + reportId);
    }

    if (rentalRepository.findById(request.getRentalId()).isEmpty()) {
      throw new NotFoundException("Rental not found with ID: " + request.getRentalId());
    }
    if (vehicleRepository.findById(request.getVehicleId().longValue()).isEmpty()) {
      throw new NotFoundException("Vehicle not found with ID: " + request.getVehicleId());
    }
    if (userRepository.findById(request.getUserId().longValue()).isEmpty()) {
      throw new NotFoundException("User not found with ID: " + request.getUserId());
    }
    if (request.getCheckId() != null
        && vehicleCheckRepository.findById(request.getCheckId()).isEmpty()) {
      throw new NotFoundException("VehicleCheck not found with ID: " + request.getCheckId());
    }

    IncidentReports existingIncidentReport = incidentReportsOptional.get();
    incidentReportMapper.updateIncidentReports(request, existingIncidentReport);
    return incidentReportMapper.toIncidentReportResponse(
        incidentReportRepository.save(existingIncidentReport));
  }

  @Override
  public void deleteIncidentReport(Long reportId) {
    if (incidentReportRepository.findById(reportId).isEmpty()) {
      throw new NotFoundException("IncidentReport not found with ID: " + reportId);
    }
    incidentReportRepository.deleteById(reportId);
  }
}

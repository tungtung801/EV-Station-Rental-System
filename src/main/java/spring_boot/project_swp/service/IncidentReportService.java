package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;

public interface IncidentReportService {

  IncidentReportResponse createIncidentReport(IncidentReportRequest request);

  List<IncidentReportResponse> getAllIncidentReports();

  IncidentReportResponse getIncidentReportById(Long reportId);

  IncidentReportResponse updateIncidentReport(Long reportId, IncidentReportRequest request);

  void deleteIncidentReport(Long reportId);
}

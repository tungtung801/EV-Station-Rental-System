package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.service.IncidentReportService;

@RestController
@RequestMapping("/api/incident-reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Incident Report APIs", description = "APIs for managing incident reports")
public class IncidentReportController {

  final IncidentReportService incidentReportService;

  @PostMapping
  @Operation(
      summary = "Create a new incident report",
      description = "Creates a new incident report with the provided details.")
  public ResponseEntity<IncidentReportResponse> createIncidentReport(
      @RequestBody @Valid IncidentReportRequest request) {
    return new ResponseEntity<>(
        incidentReportService.createIncidentReport(request), HttpStatus.CREATED);
  }

  @GetMapping
  @Operation(
      summary = "Get all incident reports",
      description = "Retrieves a list of all incident reports.")
  public ResponseEntity<List<IncidentReportResponse>> getAllIncidentReports() {
    return new ResponseEntity<>(incidentReportService.getAllIncidentReports(), HttpStatus.OK);
  }

  @GetMapping("/{reportId}")
  @Operation(
      summary = "Get incident report by ID",
      description = "Retrieves an incident report by its unique ID.")
  public ResponseEntity<IncidentReportResponse> getIncidentReportById(@PathVariable Long reportId) {
    return new ResponseEntity<>(
        incidentReportService.getIncidentReportById(reportId), HttpStatus.OK);
  }

  @PutMapping("/{reportId}")
  @Operation(
      summary = "Update an existing incident report",
      description = "Updates an existing incident report with the provided details.")
  public ResponseEntity<IncidentReportResponse> updateIncidentReport(
      @PathVariable Long reportId, @RequestBody @Valid IncidentReportRequest request) {
    return new ResponseEntity<>(
        incidentReportService.updateIncidentReport(reportId, request), HttpStatus.OK);
  }

  @DeleteMapping("/{reportId}")
  @Operation(
      summary = "Delete an incident report",
      description = "Deletes an incident report by its ID.")
  public ResponseEntity<Void> deleteIncidentReport(@PathVariable Long reportId) {
    incidentReportService.deleteIncidentReport(reportId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

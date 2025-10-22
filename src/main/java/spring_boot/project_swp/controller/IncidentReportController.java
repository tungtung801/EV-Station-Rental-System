package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.IncidentReportRequest;
import spring_boot.project_swp.dto.response.IncidentReportResponse;
import spring_boot.project_swp.service.IncidentReportService;

import java.util.List;

@RestController
@RequestMapping("/api/incident-reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IncidentReportController {

    final IncidentReportService incidentReportService;

    @PostMapping
    public ResponseEntity<IncidentReportResponse> createIncidentReport(@RequestBody @Valid IncidentReportRequest request) {
        return new ResponseEntity<>(incidentReportService.createIncidentReport(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IncidentReportResponse>> getAllIncidentReports() {
        return new ResponseEntity<>(incidentReportService.getAllIncidentReports(), HttpStatus.OK);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<IncidentReportResponse> getIncidentReportById(@PathVariable Long reportId) {
        return new ResponseEntity<>(incidentReportService.getIncidentReportById(reportId), HttpStatus.OK);
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<IncidentReportResponse> updateIncidentReport(@PathVariable Long reportId, @RequestBody @Valid IncidentReportRequest request) {
        return new ResponseEntity<>(incidentReportService.updateIncidentReport(reportId, request), HttpStatus.OK);
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteIncidentReport(@PathVariable Long reportId) {
        incidentReportService.deleteIncidentReport(reportId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
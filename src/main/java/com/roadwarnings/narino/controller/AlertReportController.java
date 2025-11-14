package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.AlertReportRequestDTO;
import com.roadwarnings.narino.dto.response.AlertReportResponseDTO;
import com.roadwarnings.narino.service.AlertReportService;
import com.roadwarnings.narino.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertReportController {

    private final AlertReportService alertReportService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping
    public ResponseEntity<AlertReportResponseDTO> createReport(
            @Valid @RequestBody AlertReportRequestDTO request) {

        String username = authenticationUtil.getAuthenticatedUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertReportService.createReport(username, request));
    }

    @GetMapping
    public ResponseEntity<List<AlertReportResponseDTO>> getAllReports() {
        return ResponseEntity.ok(alertReportService.getAllReports());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<AlertReportResponseDTO>> getAllReportsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(alertReportService.getAllReportsPaginated(pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AlertReportResponseDTO>> getPendingReports() {
        return ResponseEntity.ok(alertReportService.getPendingReports());
    }

    @GetMapping("/pending/paginated")
    public ResponseEntity<Page<AlertReportResponseDTO>> getPendingReportsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(alertReportService.getPendingReportsPaginated(pageable));
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<AlertReportResponseDTO>> getReportsByAlertId(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertReportService.getReportsByAlertId(alertId));
    }

    @GetMapping("/alert/{alertId}/count")
    public ResponseEntity<Long> getReportCountByAlertId(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertReportService.getReportCountByAlertId(alertId));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<AlertReportResponseDTO>> getMyReports() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(alertReportService.getReportsByUserId(userId));
    }

    @PatchMapping("/{reportId}/review")
    public ResponseEntity<AlertReportResponseDTO> reviewReport(
            @PathVariable Long reportId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String reviewNotes) {

        String username = authenticationUtil.getAuthenticatedUsername();
        return ResponseEntity.ok(alertReportService.reviewReport(reportId, username, approve, reviewNotes));
    }
}

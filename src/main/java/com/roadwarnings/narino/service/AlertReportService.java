package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.AlertReportRequestDTO;
import com.roadwarnings.narino.dto.response.AlertReportResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.AlertReport;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.AlertReportRepository;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertReportService {

    private final AlertReportRepository alertReportRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String REPORT_NOT_FOUND = "Reporte no encontrado";
    private static final int AUTO_REJECT_THRESHOLD = 5; // Rechazar automáticamente después de 5 reportes

    public AlertReportResponseDTO createReport(String username, AlertReportRequestDTO request) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Alert alert = alertRepository.findById(request.getAlertId())
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));

        // Verificar si el usuario ya reportó esta alerta
        if (alertReportRepository.existsByAlertIdAndReporterId(alert.getId(), reporter.getId())) {
            throw new RuntimeException("Ya has reportado esta alerta");
        }

        AlertReport report = AlertReport.builder()
                .alert(alert)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .reviewed(false)
                .build();

        report = alertReportRepository.save(report);
        log.info("Reporte creado por usuario {} para alerta {}", username, alert.getId());

        // Verificar si la alerta debe ser auto-rechazada
        checkAutoReject(alert);

        return mapToResponseDTO(report);
    }

    public List<AlertReportResponseDTO> getAllReports() {
        return alertReportRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<AlertReportResponseDTO> getAllReportsPaginated(Pageable pageable) {
        return alertReportRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    public List<AlertReportResponseDTO> getPendingReports() {
        return alertReportRepository.findByReviewed(false).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<AlertReportResponseDTO> getPendingReportsPaginated(Pageable pageable) {
        return alertReportRepository.findByReviewed(false, pageable)
                .map(this::mapToResponseDTO);
    }

    public List<AlertReportResponseDTO> getReportsByAlertId(Long alertId) {
        return alertReportRepository.findByAlertId(alertId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<AlertReportResponseDTO> getReportsByUserId(Long userId) {
        return alertReportRepository.findByReporterId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public AlertReportResponseDTO reviewReport(Long reportId, String reviewerUsername, boolean approve, String reviewNotes) {
        AlertReport report = alertReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(REPORT_NOT_FOUND));

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        report.setReviewed(true);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNotes(reviewNotes);

        if (approve) {
            // Si se aprueba el reporte, cambiar estado de la alerta
            Alert alert = report.getAlert();
            alert.setStatus(AlertStatus.REJECTED);
            alertRepository.save(alert);
            log.info("Alerta {} rechazada por reporte aprobado", alert.getId());
        }

        report = alertReportRepository.save(report);
        log.info("Reporte {} revisado por {}", reportId, reviewerUsername);

        return mapToResponseDTO(report);
    }

    public Long getReportCountByAlertId(Long alertId) {
        return alertReportRepository.countByAlertId(alertId);
    }

    public Long getPendingReportCountByAlertId(Long alertId) {
        return alertReportRepository.countPendingByAlertId(alertId);
    }

    private void checkAutoReject(Alert alert) {
        Long reportCount = alertReportRepository.countPendingByAlertId(alert.getId());

        if (reportCount >= AUTO_REJECT_THRESHOLD) {
            alert.setStatus(AlertStatus.UNDER_REVIEW);
            alertRepository.save(alert);
            log.warn("Alerta {} puesta en revisión automática por {} reportes", alert.getId(), reportCount);
        }
    }

    private AlertReportResponseDTO mapToResponseDTO(AlertReport report) {
        return AlertReportResponseDTO.builder()
                .id(report.getId())
                .alertId(report.getAlert().getId())
                .alertTitle(report.getAlert().getTitle())
                .reporterId(report.getReporter().getId())
                .reporterUsername(report.getReporter().getUsername())
                .reason(report.getReason())
                .description(report.getDescription())
                .reviewed(report.getReviewed())
                .reviewedById(report.getReviewedBy() != null ? report.getReviewedBy().getId() : null)
                .reviewedByUsername(report.getReviewedBy() != null ? report.getReviewedBy().getUsername() : null)
                .reviewedAt(report.getReviewedAt())
                .reviewNotes(report.getReviewNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

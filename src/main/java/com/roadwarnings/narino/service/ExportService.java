package com.roadwarnings.narino.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.ExportFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public byte[] exportAlerts(List<AlertaResponseDTO> alerts, ExportFormat format) {
        return switch (format) {
            case CSV -> exportAlertsToCsv(alerts);
            case JSON -> exportAlertsToJson(alerts);
        };
    }

    private byte[] exportAlertsToCsv(List<AlertaResponseDTO> alerts) {
        StringBuilder sb = new StringBuilder();

        // Cabecera
        sb.append("id,type,title,description,latitude,longitude,location,municipality,")
                .append("severity,status,username,userId,upvotes,downvotes,createdAt,updatedAt,expiresAt")
                .append("\n");

        for (AlertaResponseDTO alert : alerts) {
            sb.append(csv(alert.getId()))
                    .append(csv(alert.getType()))
                    .append(csv(alert.getTitle()))
                    .append(csv(alert.getDescription()))
                    .append(csv(alert.getLatitude()))
                    .append(csv(alert.getLongitude()))
                    .append(csv(alert.getLocation()))
                    .append(csv(alert.getMunicipality()))
                    .append(csv(alert.getSeverity()))
                    .append(csv(alert.getStatus()))
                    .append(csv(alert.getUsername()))
                    .append(csv(alert.getUserId()))
                    .append(csv(alert.getUpvotes()))
                    .append(csv(alert.getDownvotes()))
                    .append(csv(formatDate(alert.getCreatedAt())))
                    .append(csv(formatDate(alert.getUpdatedAt())))
                    .append(csv(formatDate(alert.getExpiresAt())));

            // quitar coma final y agregar nueva línea
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, '\n');
            } else {
                sb.append("\n");
            }
        }

        return sb.toString().getBytes();
    }

    private byte[] exportAlertsToJson(List<AlertaResponseDTO> alerts) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(alerts);
            return json.getBytes();
        } catch (JsonProcessingException e) {
            log.error("Error exportando alertas a JSON: {}", e.getMessage());
            throw new RuntimeException("Error al generar JSON de exportación", e);
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        // Escapar comillas y envolver en comillas
        text = text.replace("\"", "\"\"");
        return "\"" + text + "\",";
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime != null ? DATE_FORMATTER.format(dateTime) : null;
    }
}


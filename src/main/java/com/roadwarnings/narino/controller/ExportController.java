package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.AlertSearchDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.ExportFormat;
import com.roadwarnings.narino.service.AlertSearchService;
import com.roadwarnings.narino.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ExportController {

    private final AlertSearchService alertSearchService;
    private final ExportService exportService;

    /**
     * Exporta alertas usando los mismos filtros de AlertSearchDTO.
     * POST /api/export/alerts?format=CSV|JSON
     */
    @PostMapping("/alerts")
    public ResponseEntity<byte[]> exportAlerts(
            @RequestBody AlertSearchDTO searchDTO,
            @RequestParam(defaultValue = "CSV") ExportFormat format) {

        // Para exportar, recuperamos un número razonable de resultados
        if (searchDTO.getPage() == null) {
            searchDTO.setPage(0);
        }
        if (searchDTO.getSize() == null || searchDTO.getSize() > 10_000) {
            searchDTO.setSize(10_000);
        }

        Page<AlertaResponseDTO> page = alertSearchService.searchAlerts(searchDTO);

        byte[] bytes = exportService.exportAlerts(page.getContent(), format);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String baseName = "alerts-" + timestamp;

        String filename = switch (format) {
            case CSV -> baseName + ".csv";
            case JSON -> baseName + ".json";
        };

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        MediaType mediaType = switch (format) {
            case CSV -> MediaType.parseMediaType("text/csv");
            case JSON -> MediaType.APPLICATION_JSON;
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

        log.info("Exportando {} alertas en formato {}", page.getNumberOfElements(), format);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(bytes);
    }
}


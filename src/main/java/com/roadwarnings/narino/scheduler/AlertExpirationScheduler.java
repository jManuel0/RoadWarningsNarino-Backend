package com.roadwarnings.narino.scheduler;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertExpirationScheduler {

    private final AlertRepository alertRepository;

    /**
     * Ejecuta cada hora para expirar alertas que hayan pasado su fecha de expiración
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    @Transactional
    public void expireAlerts() {
        log.info("Iniciando proceso de expiración de alertas");

        LocalDateTime now = LocalDateTime.now();

        // Buscar alertas activas que ya expiraron
        List<Alert> expiredAlerts = alertRepository.findAll().stream()
                .filter(alert -> alert.getStatus() == AlertStatus.ACTIVE)
                .filter(alert -> alert.getExpiresAt() != null)
                .filter(alert -> alert.getExpiresAt().isBefore(now))
                .toList();

        if (!expiredAlerts.isEmpty()) {
            for (Alert alert : expiredAlerts) {
                alert.setStatus(AlertStatus.EXPIRED);
                alertRepository.save(alert);
                log.info("Alerta {} expirada automáticamente", alert.getId());
            }

            log.info("Total de alertas expiradas: {}", expiredAlerts.size());
        } else {
            log.info("No hay alertas para expirar");
        }
    }

    /**
     * Ejecuta cada día a las 2 AM para limpiar alertas muy antiguas
     */
    @Scheduled(cron = "0 0 2 * * *") // Todos los días a las 2 AM
    @Transactional
    public void cleanupOldAlerts() {
        log.info("Iniciando limpieza de alertas antiguas");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Buscar alertas resueltas o expiradas de hace más de 30 días
        List<Alert> oldAlerts = alertRepository.findAll().stream()
                .filter(alert -> alert.getStatus() == AlertStatus.RESOLVED ||
                               alert.getStatus() == AlertStatus.EXPIRED)
                .filter(alert -> alert.getUpdatedAt() != null &&
                               alert.getUpdatedAt().isBefore(thirtyDaysAgo))
                .toList();

        if (!oldAlerts.isEmpty()) {
            // Puedes decidir eliminarlas o solo marcarlas
            // Por ahora solo las dejamos como están, pero podrías eliminarlas:
            // alertRepository.deleteAll(oldAlerts);
            log.info("Se encontraron {} alertas antiguas (no eliminadas, solo registradas)", oldAlerts.size());
        } else {
            log.info("No hay alertas antiguas para limpiar");
        }
    }

    /**
     * Ejecuta cada 10 minutos para auto-resolver alertas con muchos reportes
     */
    @Scheduled(cron = "0 */10 * * * *") // Cada 10 minutos
    @Transactional
    public void checkHighlyReportedAlerts() {
        log.debug("Verificando alertas con muchos reportes");

        // Esta lógica se implementará más adelante con el servicio de reportes
        // Por ahora solo está el scheduler preparado
    }
}

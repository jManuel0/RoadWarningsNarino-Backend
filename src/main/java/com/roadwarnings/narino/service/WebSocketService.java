package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.dto.response.CommentResponseDTO;
import com.roadwarnings.narino.dto.websocket.AlertWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una nueva alerta a todos los usuarios conectados
     */
    public void broadcastNewAlert(AlertaResponseDTO alert) {
        AlertWebSocketMessage message = AlertWebSocketMessage.builder()
                .action("CREATED")
                .alertId(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .username(alert.getUsername())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", message);
        log.info("Nueva alerta broadcast a WebSocket: {}", alert.getId());
    }

    /**
     * Envía actualización de una alerta a todos los usuarios conectados
     */
    public void broadcastAlertUpdate(AlertaResponseDTO alert) {
        AlertWebSocketMessage message = AlertWebSocketMessage.builder()
                .action("UPDATED")
                .alertId(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .username(alert.getUsername())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", message);
        log.info("Actualización de alerta broadcast a WebSocket: {}", alert.getId());
    }

    /**
     * Envía eliminación de una alerta a todos los usuarios conectados
     */
    public void broadcastAlertDeletion(Long alertId) {
        AlertWebSocketMessage message = AlertWebSocketMessage.builder()
                .action("DELETED")
                .alertId(alertId)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", message);
        log.info("Eliminación de alerta broadcast a WebSocket: {}", alertId);
    }

    /**
     * Envía cambio de estado de una alerta a todos los usuarios conectados
     */
    public void broadcastAlertStatusChange(AlertaResponseDTO alert) {
        AlertWebSocketMessage message = AlertWebSocketMessage.builder()
                .action("STATUS_CHANGED")
                .alertId(alert.getId())
                .status(alert.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", message);
        log.info("Cambio de estado de alerta broadcast a WebSocket: {}", alert.getId());
    }

    /**
     * Envía actualización de votos de una alerta
     */
    public void broadcastAlertVoteUpdate(Long alertId, Integer upvotes, Integer downvotes) {
        AlertWebSocketMessage message = AlertWebSocketMessage.builder()
                .action("VOTE_UPDATE")
                .alertId(alertId)
                .upvotes(upvotes)
                .downvotes(downvotes)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", message);
        log.info("Actualización de votos broadcast a WebSocket: alerta {}", alertId);
    }

    /**
     * Envía un nuevo comentario a los usuarios que siguen esa alerta
     */
    public void broadcastNewComment(CommentResponseDTO comment) {
        messagingTemplate.convertAndSend("/topic/alerts/" + comment.getAlertId() + "/comments", comment);
        log.info("Nuevo comentario broadcast a WebSocket: alerta {}", comment.getAlertId());
    }

    /**
     * Envía alertas cercanas a un usuario específico basado en su ubicación
     */
    public void sendNearbyAlertsToUser(String username, AlertaResponseDTO alert) {
        messagingTemplate.convertAndSendToUser(username, "/queue/nearby-alerts", alert);
        log.info("Alerta cercana enviada a usuario {}: {}", username, alert.getId());
    }

    /**
     * Envía una notificación personalizada a un usuario específico
     */
    public void sendPersonalNotification(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
        log.info("Notificación personal enviada a usuario: {}", username);
    }
}

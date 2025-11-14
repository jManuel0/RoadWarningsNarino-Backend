package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.response.NotificationResponseDTO;
import com.roadwarnings.narino.entity.Notification;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.NotificationType;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final String NOTIFICATION_NOT_FOUND = "Notificación no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    public NotificationResponseDTO createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long relatedEntityId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notificación creada para usuario {}: {}", userId, title);

        return mapToResponseDTO(notification);
    }

    public List<NotificationResponseDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<NotificationResponseDTO> getUserNotificationsPaginated(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponseDTO);
    }

    public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<NotificationResponseDTO> getUnreadNotificationsPaginated(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsRead(userId, false, pageable)
                .map(this::mapToResponseDTO);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public NotificationResponseDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(NOTIFICATION_NOT_FOUND));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        log.info("Notificación {} marcada como leída", notificationId);
        return mapToResponseDTO(notification);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Todas las notificaciones del usuario {} marcadas como leídas", userId);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta notificación");
        }

        notificationRepository.delete(notification);
        log.info("Notificación {} eliminada", notificationId);
    }

    public void deleteReadNotifications(Long userId) {
        notificationRepository.deleteReadNotificationsByUserId(userId);
        log.info("Notificaciones leídas del usuario {} eliminadas", userId);
    }

    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}

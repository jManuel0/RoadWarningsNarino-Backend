package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.DeviceTokenRequestDTO;
import com.roadwarnings.narino.dto.response.DeviceTokenResponseDTO;
import com.roadwarnings.narino.dto.response.NotificationResponseDTO;
import com.roadwarnings.narino.service.NotificationService;
import com.roadwarnings.narino.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(notificationService.getUserNotifications(
                getUserIdFromUsername(username)));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationResponseDTO>> getMyNotificationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = getAuthenticatedUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(notificationService.getUserNotificationsPaginated(
                getUserIdFromUsername(username), pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(
                getUserIdFromUsername(username)));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(notificationService.getUnreadCount(
                getUserIdFromUsername(username)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        String username = getAuthenticatedUsername();
        notificationService.markAllAsRead(getUserIdFromUsername(username));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        notificationService.deleteNotification(id, getUserIdFromUsername(username));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications() {
        String username = getAuthenticatedUsername();
        notificationService.deleteReadNotifications(getUserIdFromUsername(username));
        return ResponseEntity.noContent().build();
    }

    // ==================== DEVICE TOKENS ====================

    @PostMapping("/device-token")
    public ResponseEntity<DeviceTokenResponseDTO> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(pushNotificationService.registerDeviceToken(username, request));
    }

    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<Void> unregisterDeviceToken(@PathVariable String token) {
        pushNotificationService.unregisterDeviceToken(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device-tokens")
    public ResponseEntity<List<DeviceTokenResponseDTO>> getMyDeviceTokens() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(pushNotificationService.getUserDeviceTokens(
                getUserIdFromUsername(username)));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private Long getUserIdFromUsername(String username) {
        // Este método debería obtener el ID del usuario desde el repositorio
        // Por simplicidad, retornamos un valor temporal
        // TODO: Implementar correctamente
        return 1L;
    }
}

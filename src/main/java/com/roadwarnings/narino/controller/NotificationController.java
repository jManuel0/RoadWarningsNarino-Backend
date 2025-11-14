package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.DeviceTokenRequestDTO;
import com.roadwarnings.narino.dto.response.DeviceTokenResponseDTO;
import com.roadwarnings.narino.dto.response.NotificationResponseDTO;
import com.roadwarnings.narino.service.NotificationService;
import com.roadwarnings.narino.service.PushNotificationService;
import com.roadwarnings.narino.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationResponseDTO>> getMyNotificationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = authenticationUtil.getAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(notificationService.getUserNotificationsPaginated(userId, pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        notificationService.deleteReadNotifications(userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== DEVICE TOKENS ====================

    @PostMapping("/device-token")
    public ResponseEntity<DeviceTokenResponseDTO> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequestDTO request) {

        String username = authenticationUtil.getAuthenticatedUsername();
        return ResponseEntity.ok(pushNotificationService.registerDeviceToken(username, request));
    }

    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<Void> unregisterDeviceToken(@PathVariable String token) {
        pushNotificationService.unregisterDeviceToken(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device-tokens")
    public ResponseEntity<List<DeviceTokenResponseDTO>> getMyDeviceTokens() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(pushNotificationService.getUserDeviceTokens(userId));
    }
}

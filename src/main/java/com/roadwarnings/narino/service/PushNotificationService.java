package com.roadwarnings.narino.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.roadwarnings.narino.dto.request.DeviceTokenRequestDTO;
import com.roadwarnings.narino.dto.response.DeviceTokenResponseDTO;
import com.roadwarnings.narino.entity.DeviceToken;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.DeviceTokenRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    public DeviceTokenResponseDTO registerDeviceToken(String username, DeviceTokenRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Si el token ya existe, actualizarlo
        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getToken())
                .orElse(DeviceToken.builder()
                        .user(user)
                        .token(request.getToken())
                        .build());

        deviceToken.setDeviceType(request.getDeviceType());
        deviceToken.setDeviceName(request.getDeviceName());
        deviceToken.setIsActive(true);

        deviceToken = deviceTokenRepository.save(deviceToken);
        log.info("Token de dispositivo registrado para usuario: {}", username);

        return mapToResponseDTO(deviceToken);
    }

    public void unregisterDeviceToken(String token) {
        deviceTokenRepository.deactivateToken(token);
        log.info("Token de dispositivo desactivado: {}", token);
    }

    public void unregisterAllUserTokens(Long userId) {
        deviceTokenRepository.deactivateAllTokensByUserId(userId);
        log.info("Todos los tokens del usuario {} desactivados", userId);
    }

    public List<DeviceTokenResponseDTO> getUserDeviceTokens(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActive(userId, true).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public void sendNotificationToUser(Long userId, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase no está inicializado. No se pueden enviar notificaciones push.");
            return;
        }

        List<String> tokens = deviceTokenRepository.findActiveTokensByUserId(userId);

        if (tokens.isEmpty()) {
            log.info("No hay tokens activos para el usuario {}", userId);
            return;
        }

        sendMulticastNotification(tokens, title, body, data);
    }

    public void sendNotificationToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase no está inicializado. No se pueden enviar notificaciones push.");
            return;
        }

        for (Long userId : userIds) {
            sendNotificationToUser(userId, title, body, data);
        }
    }

    public void sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase no está inicializado. No se pueden enviar notificaciones push.");
            return;
        }

        if (tokens.isEmpty()) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(tokens);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            MulticastMessage message = messageBuilder.build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

            log.info("Notificaciones enviadas exitosamente: {} de {}",
                    response.getSuccessCount(), tokens.size());

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String token = tokens.get(i);
                        Exception exception = responses.get(i).getException();
                        log.error("Error al enviar notificación al token {}: {}",
                                token, exception.getMessage());

                        // Si el token es inválido, desactivarlo
                        if (exception instanceof FirebaseMessagingException fme && ("invalid-registration-token".equals(fme.getErrorCode()) ||
                                "registration-token-not-registered".equals(fme.getErrorCode()))) {
                                deviceTokenRepository.deactivateToken(token);
                                log.info("Token inválido desactivado: {}", token);
                            }
                        
                    }
                }
            }

        } catch (FirebaseMessagingException e) {
            log.error("Error al enviar notificaciones push: {}", e.getMessage());
        }
    }

    public void sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase no está inicializado. No se pueden enviar notificaciones push.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic(topic);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Notificación enviada al topic {}: {}", topic, response);

        } catch (FirebaseMessagingException e) {
            log.error("Error al enviar notificación al topic {}: {}", topic, e.getMessage());
        }
    }

    private DeviceTokenResponseDTO mapToResponseDTO(DeviceToken deviceToken) {
        return DeviceTokenResponseDTO.builder()
                .id(deviceToken.getId())
                .userId(deviceToken.getUser().getId())
                .deviceType(deviceToken.getDeviceType())
                .deviceName(deviceToken.getDeviceName())
                .isActive(deviceToken.getIsActive())
                .createdAt(deviceToken.getCreatedAt())
                .lastUsed(deviceToken.getLastUsed())
                .build();
    }
}

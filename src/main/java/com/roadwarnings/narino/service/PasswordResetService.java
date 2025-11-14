package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.ChangePasswordRequestDTO;
import com.roadwarnings.narino.dto.request.ForgotPasswordRequestDTO;
import com.roadwarnings.narino.dto.request.ResetPasswordRequestDTO;
import com.roadwarnings.narino.entity.PasswordResetToken;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.PasswordResetTokenRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String INVALID_TOKEN = "Token inválido o expirado";
    private static final String PASSWORDS_DO_NOT_MATCH = "Las contraseñas no coinciden";
    private static final String INCORRECT_PASSWORD = "Contraseña actual incorrecta";

    public void changePassword(String username, ChangePasswordRequestDTO request) {
        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException(PASSWORDS_DO_NOT_MATCH);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException(INCORRECT_PASSWORD);
        }

        // Cambiar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Contraseña cambiada para usuario: {}", username);
    }

    public void initiatePasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUserId(user.getId());

        // Generar nuevo token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Enviar email con el token
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getUsername(),
                token
        );

        log.info("Token de recuperación generado para usuario: {}", user.getUsername());
    }

    public void resetPassword(ResetPasswordRequestDTO request) {
        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException(PASSWORDS_DO_NOT_MATCH);
        }

        // Buscar token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException(INVALID_TOKEN));

        // Verificar que no esté expirado ni usado
        if (resetToken.isExpired() || resetToken.getUsed()) {
            throw new RuntimeException(INVALID_TOKEN);
        }

        // Cambiar contraseña
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña reseteada para usuario: {}", user.getUsername());
    }

    /**
     * Limpia tokens expirados y usados cada día a las 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Limpiando tokens de recuperación expirados");
        tokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
    }
}

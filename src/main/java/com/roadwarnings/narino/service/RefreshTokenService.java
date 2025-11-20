package com.roadwarnings.narino.service;

import com.roadwarnings.narino.entity.RefreshToken;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.exception.BadRequestException;
import com.roadwarnings.narino.repository.RefreshTokenRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshTokenDurationMs; // 7 días por defecto

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        // Eliminar tokens anteriores del usuario
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token expirado. Por favor inicia sesión nuevamente.");
        }
        if (token.isRevoked()) {
            throw new BadRequestException("Refresh token revocado. Por favor inicia sesión nuevamente.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.revokeToken(token, Instant.now());
    }

    @Transactional
    public void revokeUserTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        refreshTokenRepository.deleteByUser(user);
    }

    // Limpieza automática de tokens expirados cada día a las 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpieza de refresh tokens expirados");
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Limpieza de refresh tokens completada");
    }
}

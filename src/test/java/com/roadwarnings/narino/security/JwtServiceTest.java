package com.roadwarnings.narino.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Inicializar con un secreto de prueba (mínimo 32 caracteres)
        String testSecret = "test-secret-key-for-jwt-must-be-at-least-32-chars-long";
        long testExpiration = 3600000L; // 1 hora

        jwtService = new JwtService(testSecret, testExpiration);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // Act
        String token = jwtService.generateToken("testuser");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    void extractUsername_ShouldExtractCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken("testuser");

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void isTokenValid_WhenTokenIsValid_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken("testuser");

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WhenUsernameDoesNotMatch_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken("testuser");
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WhenTokenIsInvalid_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUsername_WhenTokenIsInvalid_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokensForDifferentUsers() {
        // Act
        String token1 = jwtService.generateToken("user1");
        String token2 = jwtService.generateToken("user2");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_MultipleCallsShouldGenerateDifferentTokens() throws InterruptedException {
        // Arrange
        String token1 = jwtService.generateToken("testuser");

        // Esperar un momento para que cambie el timestamp
        Thread.sleep(10);

        String token2 = jwtService.generateToken("testuser");

        // Assert
        // Los tokens deben ser diferentes porque tienen diferentes timestamps
        assertNotEquals(token1, token2);
    }
}

package com.roadwarnings.narino.util;

import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para obtener información del usuario autenticado
 */
@Component
@RequiredArgsConstructor
public class AuthenticationUtil {

    private final UserRepository userRepository;

    /**
     * Obtiene el username del usuario actualmente autenticado
     * @return username del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        String username = authentication.getName();

        if (username == null || username.equals("anonymousUser")) {
            throw new IllegalStateException("Usuario no autenticado o anónimo");
        }

        return username;
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado
     * @return ID del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     * @throws RuntimeException si el usuario no existe en la base de datos
     */
    public Long getAuthenticatedUserId() {
        String username = getAuthenticatedUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario autenticado no encontrado en la base de datos: " + username));

        return user.getId();
    }

    /**
     * Obtiene la entidad User completa del usuario autenticado
     * @return Usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     * @throws RuntimeException si el usuario no existe en la base de datos
     */
    public User getAuthenticatedUser() {
        String username = getAuthenticatedUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario autenticado no encontrado en la base de datos: " + username));
    }

    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean isAuthenticated() {
        try {
            getAuthenticatedUsername();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Obtiene el ID del usuario desde el username
     * @param username nombre de usuario
     * @return ID del usuario
     * @throws RuntimeException si el usuario no existe
     */
    public Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + username));

        return user.getId();
    }
}

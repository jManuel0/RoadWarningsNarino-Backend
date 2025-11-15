package com.roadwarnings.narino.util;

import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.UserRole;
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

    /**
     * Verifica si el usuario autenticado tiene un rol específico
     * @param role rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(UserRole role) {
        try {
            User user = getAuthenticatedUser();
            return user.getRole() == role;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario autenticado es administrador
     * @return true si es administrador, false en caso contrario
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Verifica si el usuario autenticado es moderador
     * @return true si es moderador, false en caso contrario
     */
    public boolean isModerator() {
        return hasRole(UserRole.MODERATOR);
    }

    /**
     * Verifica si el usuario autenticado es administrador o moderador
     * @return true si es admin o moderador, false en caso contrario
     */
    public boolean isAdminOrModerator() {
        return isAdmin() || isModerator();
    }

    /**
     * Verifica si el usuario autenticado tiene autoridad especial
     * @return true si tiene el rol AUTHORITY, false en caso contrario
     */
    public boolean hasAuthority() {
        return hasRole(UserRole.AUTHORITY);
    }
}

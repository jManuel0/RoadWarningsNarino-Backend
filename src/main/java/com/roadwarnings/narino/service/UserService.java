package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.UserUpdateRequestDTO;
import com.roadwarnings.narino.dto.response.UserResponseDTO;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return mapToResponseDTO(user);
    }

    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return mapToResponseDTO(user);
    }

    public UserResponseDTO getMyProfile(String username) {
        return getUserByUsername(username);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO request, String authenticatedUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Solo el mismo usuario puede actualizarse
        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new UnauthorizedException("No tienes permiso para actualizar este usuario");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Verificar que el email no esté en uso por otro usuario
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("El email ya está en uso");
                }
            });
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getPreferredTheme() != null) {
            user.setPreferredTheme(request.getPreferredTheme());
        }

        user = userRepository.save(user);
        log.info("Usuario {} actualizado", user.getUsername());

        return mapToResponseDTO(user);
    }

    public UserResponseDTO updateMyProfile(UserUpdateRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return updateUser(user.getId(), request, username);
    }

    public void deleteUser(Long id, String authenticatedUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Solo el mismo usuario puede eliminarse
        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este usuario");
        }

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Usuario {} desactivado", user.getUsername());
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .preferredTheme(user.getPreferredTheme())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

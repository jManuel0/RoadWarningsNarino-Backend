package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.SavedPlaceRequestDTO;
import com.roadwarnings.narino.dto.response.SavedPlaceResponseDTO;
import com.roadwarnings.narino.entity.SavedPlace;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.SavedPlaceType;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.SavedPlaceRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;
    private final UserRepository userRepository;

    private static final int MAX_SAVED_PLACES = 50;
    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String PLACE_NOT_FOUND = "Lugar guardado no encontrado";

    /**
     * Obtener todos los lugares guardados de un usuario
     */
    public List<SavedPlaceResponseDTO> getUserSavedPlaces(Long userId, String authenticatedUsername) {
        validateUserAccess(userId, authenticatedUsername);

        List<SavedPlace> places = savedPlaceRepository.findByUserId(userId);
        return places.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crear un nuevo lugar guardado
     */
    public SavedPlaceResponseDTO createSavedPlace(Long userId, SavedPlaceRequestDTO request, String authenticatedUsername) {
        validateUserAccess(userId, authenticatedUsername);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        // Validar límite de lugares guardados
        long count = savedPlaceRepository.countByUserId(userId);
        if (count >= MAX_SAVED_PLACES) {
            throw new RuntimeException("Has alcanzado el límite máximo de " + MAX_SAVED_PLACES + " lugares guardados");
        }

        // Validar que solo haya un lugar de tipo HOME o WORK
        if (request.getType() == SavedPlaceType.HOME || request.getType() == SavedPlaceType.WORK) {
            if (savedPlaceRepository.existsByUserIdAndType(userId, request.getType())) {
                throw new RuntimeException("Ya tienes un lugar guardado de tipo " + request.getType());
            }
        }

        SavedPlace savedPlace = SavedPlace.builder()
                .user(user)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .type(request.getType())
                .build();

        savedPlace = savedPlaceRepository.save(savedPlace);
        log.info("Lugar guardado creado: {} para usuario: {}", savedPlace.getName(), user.getUsername());

        return mapToResponseDTO(savedPlace);
    }

    /**
     * Actualizar un lugar guardado
     */
    public SavedPlaceResponseDTO updateSavedPlace(Long userId, Long placeId, SavedPlaceRequestDTO request, String authenticatedUsername) {
        validateUserAccess(userId, authenticatedUsername);

        SavedPlace savedPlace = savedPlaceRepository.findByIdAndUserId(placeId, userId)
                .orElseThrow(() -> new RuntimeException(PLACE_NOT_FOUND));

        // Si cambia el tipo a HOME o WORK, validar que no exista otro
        if ((request.getType() == SavedPlaceType.HOME || request.getType() == SavedPlaceType.WORK)
                && savedPlace.getType() != request.getType()) {
            if (savedPlaceRepository.existsByUserIdAndType(userId, request.getType())) {
                throw new RuntimeException("Ya tienes un lugar guardado de tipo " + request.getType());
            }
        }

        savedPlace.setName(request.getName());
        savedPlace.setAddress(request.getAddress());
        savedPlace.setLatitude(request.getLatitude());
        savedPlace.setLongitude(request.getLongitude());
        savedPlace.setType(request.getType());

        savedPlace = savedPlaceRepository.save(savedPlace);
        log.info("Lugar guardado actualizado: {} para usuario: {}", savedPlace.getName(), userId);

        return mapToResponseDTO(savedPlace);
    }

    /**
     * Eliminar un lugar guardado
     */
    public void deleteSavedPlace(Long userId, Long placeId, String authenticatedUsername) {
        validateUserAccess(userId, authenticatedUsername);

        SavedPlace savedPlace = savedPlaceRepository.findByIdAndUserId(placeId, userId)
                .orElseThrow(() -> new RuntimeException(PLACE_NOT_FOUND));

        savedPlaceRepository.delete(savedPlace);
        log.info("Lugar guardado eliminado: {} para usuario: {}", savedPlace.getName(), userId);
    }

    /**
     * Obtener un lugar guardado por ID
     */
    public SavedPlaceResponseDTO getSavedPlaceById(Long userId, Long placeId, String authenticatedUsername) {
        validateUserAccess(userId, authenticatedUsername);

        SavedPlace savedPlace = savedPlaceRepository.findByIdAndUserId(placeId, userId)
                .orElseThrow(() -> new RuntimeException(PLACE_NOT_FOUND));

        return mapToResponseDTO(savedPlace);
    }

    // ==== Helpers ====

    private void validateUserAccess(Long userId, String authenticatedUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new UnauthorizedException("No tienes permiso para acceder a estos lugares guardados");
        }
    }

    private SavedPlaceResponseDTO mapToResponseDTO(SavedPlace savedPlace) {
        return SavedPlaceResponseDTO.builder()
                .id(savedPlace.getId())
                .userId(savedPlace.getUser().getId())
                .name(savedPlace.getName())
                .address(savedPlace.getAddress())
                .lat(savedPlace.getLatitude())
                .lng(savedPlace.getLongitude())
                .type(savedPlace.getType())
                .createdAt(savedPlace.getCreatedAt())
                .build();
    }
}

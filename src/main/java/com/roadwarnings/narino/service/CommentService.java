package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.CommentRequestDTO;
import com.roadwarnings.narino.dto.response.CommentResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.Comment;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.CommentRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final UserStatisticsService statisticsService;
    private final BadgeService badgeService;
    private final WebSocketService webSocketService;
    private final ReputationService reputationService;

    private static final String COMMENT_NOT_FOUND = "Comentario no encontrado";
    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    public CommentResponseDTO createComment(CommentRequestDTO request, String username) {
        log.info("Creando comentario para alerta {} por usuario: {}", request.getAlertId(), username);

        Alert alert = alertRepository.findById(request.getAlertId())
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .alert(alert)
                .user(user)
                .content(request.getContent())
                .helpfulCount(0)
                .build();

        comment = commentRepository.save(comment);
        log.info("Comentario creado con ID: {}", comment.getId());

        // Actualizar estadísticas del usuario
        statisticsService.incrementCommentPosted(user.getId());
        badgeService.checkAndAwardBadges(user.getId());

        // Otorgar puntos de reputación
        reputationService.onCommentCreated(user.getId());

        // Broadcast a través de WebSocket
        CommentResponseDTO response = mapToResponseDTO(comment);
        webSocketService.broadcastNewComment(response);

        return response;
    }

    public List<CommentResponseDTO> getCommentsByAlertId(Long alertId) {
        log.info("Obteniendo comentarios para alerta: {}", alertId);

        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException(ALERT_NOT_FOUND);
        }

        return commentRepository.findByAlertId(alertId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<CommentResponseDTO> getCommentsByAlertIdPaginated(Long alertId, Pageable pageable) {
        log.info("Obteniendo comentarios paginados para alerta: {}", alertId);

        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException(ALERT_NOT_FOUND);
        }

        return commentRepository.findByAlertId(alertId, pageable)
                .map(this::mapToResponseDTO);
    }

    public List<CommentResponseDTO> getCommentsByUserId(Long userId) {
        log.info("Obteniendo comentarios del usuario: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        return commentRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CommentResponseDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND));

        return mapToResponseDTO(comment);
    }

    public CommentResponseDTO updateComment(Long id, CommentRequestDTO request, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND));

        validateOwnership(comment, username);

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        log.info("Comentario {} actualizado por {}", id, username);
        return mapToResponseDTO(comment);
    }

    public void deleteComment(Long id, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND));

        validateOwnership(comment, username);

        commentRepository.delete(comment);
        log.info("Comentario {} eliminado por {}", id, username);
    }

    public CommentResponseDTO incrementHelpfulCount(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND));

        comment.setHelpfulCount(comment.getHelpfulCount() + 1);
        comment = commentRepository.save(comment);

        log.info("Comentario {} marcado como útil. Total: {}", id, comment.getHelpfulCount());
        return mapToResponseDTO(comment);
    }

    /**
     * Obtiene los comentarios mas utiles para una alerta
     */
    public List<CommentResponseDTO> getTopHelpfulComments(Long alertId, Integer limit) {
        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException(ALERT_NOT_FOUND);
        }

        return commentRepository.findByAlertId(alertId).stream()
                .sorted((c1, c2) -> c2.getHelpfulCount().compareTo(c1.getHelpfulCount()))
                .limit(limit != null ? limit : 5)
                .map(this::mapToResponseDTO)
                .toList();
    }

    private void validateOwnership(Comment comment, String username) {
        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar este comentario");
        }
    }

    private CommentResponseDTO mapToResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .alertId(comment.getAlert().getId())
                .alertTitle(comment.getAlert().getTitle())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .helpfulCount(comment.getHelpfulCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

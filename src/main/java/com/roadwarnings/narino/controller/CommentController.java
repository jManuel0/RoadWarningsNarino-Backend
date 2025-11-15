package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.annotation.RateLimited;
import com.roadwarnings.narino.dto.request.CommentRequestDTO;
import com.roadwarnings.narino.dto.response.CommentResponseDTO;
import com.roadwarnings.narino.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @RateLimited(RateLimited.Type.COMMENT_CREATION)
    public ResponseEntity<CommentResponseDTO> createComment(
            @Valid @RequestBody CommentRequestDTO request) {

        String username = getAuthenticatedUsername();
        CommentResponseDTO response = commentService.createComment(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(commentService.getCommentsByAlertId(alertId));
    }

    @GetMapping("/alert/{alertId}/paginated")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByAlertPaginated(
            @PathVariable Long alertId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(commentService.getCommentsByAlertIdPaginated(alertId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(commentService.updateComment(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        commentService.deleteComment(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<CommentResponseDTO> markAsHelpful(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.incrementHelpfulCount(id));
    }

    @GetMapping("/alert/{alertId}/top-helpful")
    public ResponseEntity<List<CommentResponseDTO>> getTopHelpfulComments(
            @PathVariable Long alertId,
            @RequestParam(defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(commentService.getTopHelpfulComments(alertId, limit));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        return authentication.getName();
    }
}

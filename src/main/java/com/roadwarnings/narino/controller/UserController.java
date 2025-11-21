package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.ChangePasswordRequestDTO;
import com.roadwarnings.narino.dto.request.ForgotPasswordRequestDTO;
import com.roadwarnings.narino.dto.request.ResetPasswordRequestDTO;
import com.roadwarnings.narino.dto.request.UserUpdateRequestDTO;
import com.roadwarnings.narino.dto.response.UserResponseDTO;
import com.roadwarnings.narino.service.PasswordResetService;
import com.roadwarnings.narino.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(userService.getMyProfile(username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(userService.updateUser(id, request, username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @Valid @RequestBody UserUpdateRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(userService.updateMyProfile(request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        userService.deleteUser(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        String username = getAuthenticatedUsername();
        passwordResetService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.initiatePasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) throws IOException {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(userService.uploadProfilePicture(username, file));
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<UserResponseDTO> deleteProfilePicture() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(userService.deleteProfilePicture(username));
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

package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.auth.AuthResponse;
import com.roadwarnings.narino.dto.auth.LoginRequest;
import com.roadwarnings.narino.dto.auth.RegisterRequest;
import com.roadwarnings.narino.entity.EmailVerificationToken;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.UserRole;
import com.roadwarnings.narino.exception.BadRequestException;
import com.roadwarnings.narino.repository.EmailVerificationTokenRepository;
import com.roadwarnings.narino.repository.UserRepository;
import com.roadwarnings.narino.security.JwtService;
import com.roadwarnings.narino.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendBaseUrl;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("El username ya está en uso");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        EmailVerificationToken verificationToken =
                EmailVerificationToken.createFor(user, 24);
        emailVerificationTokenRepository.save(verificationToken);

        String verificationLink = frontendBaseUrl + "/verify-email?token=" + verificationToken.getToken();
        String subject = "Verifica tu correo electrónico";
        String body = """
                Hola %s,
                
                Gracias por registrarte en RoadWarnings Nariño.
                
                Por favor verifica tu correo haciendo clic en el siguiente enlace:
                
                %s
                
                Este enlace expira en 24 horas.
                
                Saludos,
                El equipo de RoadWarnings Nariño
                """.formatted(user.getUsername(), verificationLink);

        emailService.sendSimpleEmail(user.getEmail(), subject, body);

        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String input = request.getUsername();

        // Permitir login por email o username:
        String username = userRepository.findByEmail(input)
                .map(User::getUsername)
                .orElse(input);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException("Debes verificar tu correo electrónico antes de iniciar sesión.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );

        String token = jwtService.generateToken(auth.getName());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return emailVerificationTokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.isExpired()) {
                        return ResponseEntity.badRequest().body("El enlace de verificación ha expirado.");
                    }

                    User user = verificationToken.getUser();
                    user.setEmailVerified(true);
                    userRepository.save(user);

                    emailVerificationTokenRepository.delete(verificationToken);

                    return ResponseEntity.ok("Correo verificado correctamente.");
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("Token de verificación inválido."));
    }
}

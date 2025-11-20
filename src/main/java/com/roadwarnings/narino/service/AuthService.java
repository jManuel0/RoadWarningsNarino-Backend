package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.auth.AuthResponse;
import com.roadwarnings.narino.dto.auth.LoginRequest;
import com.roadwarnings.narino.dto.auth.RegisterRequest;
import com.roadwarnings.narino.entity.RefreshToken;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.UserRole;
import com.roadwarnings.narino.repository.UserRepository;
import com.roadwarnings.narino.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration:3600000}")
    private Long jwtExpirationMs;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username ya existe");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtExpirationMs / 1000)
                .username(user.getUsername())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        );
        authenticationManager.authenticate(authToken);

        String token = jwtService.generateToken(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtExpirationMs / 1000)
                .username(request.getUsername())
                .build();
    }
}

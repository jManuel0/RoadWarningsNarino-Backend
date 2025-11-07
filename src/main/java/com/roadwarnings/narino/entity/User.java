package com.roadwarnings.narino.entity;

import jakarta.persistence.*;
import lombok.*;
import com.roadwarnings.narino.enums.UserRole;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private Boolean isActive = true;

    @Builder.Default  // ← AGREGADO
    private String preferredTheme = "light";

    @Column(nullable = false, updatable = false)
    @Builder.Default  // ← AGREGADO
    private LocalDateTime createdAt = LocalDateTime.now();
}
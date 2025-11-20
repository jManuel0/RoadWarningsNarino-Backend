package com.roadwarnings.narino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant revokedAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

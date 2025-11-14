package com.roadwarnings.narino.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer alertsCreated = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer alertsVerified = 0; // Alertas verificadas por otros usuarios

    @Column(nullable = false)
    @Builder.Default
    private Integer commentsPosted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer upvotesReceived = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer downvotesReceived = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reportsSubmitted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer validReports = 0; // Reportes que fueron confirmados

    @Column(nullable = false)
    @Builder.Default
    private Integer reputationPoints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime lastAlertAt;

    private LocalDateTime lastCommentAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        initializeDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private void initializeDefaults() {
        if (alertsCreated == null) alertsCreated = 0;
        if (alertsVerified == null) alertsVerified = 0;
        if (commentsPosted == null) commentsPosted = 0;
        if (upvotesReceived == null) upvotesReceived = 0;
        if (downvotesReceived == null) downvotesReceived = 0;
        if (reportsSubmitted == null) reportsSubmitted = 0;
        if (validReports == null) validReports = 0;
        if (reputationPoints == null) reputationPoints = 0;
        if (level == null) level = 1;
    }
}

package com.roadwarnings.narino.entity;

import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String location;

    private String municipality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private AlertSeverity severity = AlertSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private AlertStatus status = AlertStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String imageUrl;

    // Duración estimada en minutos (opcional)
    private Integer estimatedDuration;

    // Lista de vías afectadas (opcional)
    @ElementCollection
    @CollectionTable(name = "alert_affected_roads", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "road")
    @Builder.Default
    private List<String> affectedRoads = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private Integer upvotes = 0;

    @Column(nullable = false)
    @Builder.Default  // ← AGREGADO
    private Integer downvotes = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default  // ← AGREGADO
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertMedia> media = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

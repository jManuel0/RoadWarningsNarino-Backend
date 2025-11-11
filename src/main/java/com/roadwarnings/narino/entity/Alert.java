package com.roadwarnings.narino.entity;

import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Dirección o referencia
    private String location;

    // Municipio (Pasto, Ipiales, Tumaco, etc.)
    private String municipality;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.ACTIVE;

    private String imageUrl;

    private Integer upvotes = 0;
    private Integer downvotes = 0;

    // Duración estimada en minutos
    private Integer estimatedDuration;

    // Vías afectadas, separadas por comas (ej: "Ruta 25,Calle 18")
    @Column(length = 500)
    private String affectedRoads;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

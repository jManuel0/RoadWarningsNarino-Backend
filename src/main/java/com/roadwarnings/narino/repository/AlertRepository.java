package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus active);

    List<Alert> findByType(AlertType type);

    List<Alert> findByUserId(Long userId);

    @Query("SELECT a FROM Alert a WHERE a.status = :status AND a.createdAt > :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(@Param("status") AlertStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM Alert a WHERE a.status = com.roadwarnings.narino.enums.AlertStatus.ACTIVE " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(a.latitude)) * " +
           "cos(radians(a.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
           "sin(radians(a.latitude)))) < :radius ORDER BY a.createdAt DESC")
    List<Alert> findNearbyAlerts(@Param("lat") Double latitude,
                                  @Param("lon") Double longitude,
                                  @Param("radius") Double radiusKm);

    Alert save(Alert alert);
}
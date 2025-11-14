package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.FavoriteRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

    List<FavoriteRoute> findByUserId(Long userId);

    Page<FavoriteRoute> findByUserId(Long userId, Pageable pageable);

    List<FavoriteRoute> findByRouteId(Long routeId);

    Optional<FavoriteRoute> findByUserIdAndRouteId(Long userId, Long routeId);

    boolean existsByUserIdAndRouteId(Long userId, Long routeId);

    void deleteByUserIdAndRouteId(Long userId, Long routeId);

    @Query("SELECT COUNT(fr) FROM FavoriteRoute fr WHERE fr.route.id = :routeId")
    Long countByRouteId(@Param("routeId") Long routeId);

    @Query("SELECT fr FROM FavoriteRoute fr WHERE fr.user.id = :userId AND fr.notificationsEnabled = true")
    List<FavoriteRoute> findByUserIdAndNotificationsEnabled(@Param("userId") Long userId);
}

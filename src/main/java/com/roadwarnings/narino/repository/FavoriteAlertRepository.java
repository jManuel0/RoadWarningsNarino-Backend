package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.FavoriteAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteAlertRepository extends JpaRepository<FavoriteAlert, Long> {

    List<FavoriteAlert> findByUserId(Long userId);

    Page<FavoriteAlert> findByUserId(Long userId, Pageable pageable);

    List<FavoriteAlert> findByAlertId(Long alertId);

    Optional<FavoriteAlert> findByUserIdAndAlertId(Long userId, Long alertId);

    boolean existsByUserIdAndAlertId(Long userId, Long alertId);

    void deleteByUserIdAndAlertId(Long userId, Long alertId);

    @Query("SELECT COUNT(fa) FROM FavoriteAlert fa WHERE fa.alert.id = :alertId")
    Long countByAlertId(@Param("alertId") Long alertId);
}

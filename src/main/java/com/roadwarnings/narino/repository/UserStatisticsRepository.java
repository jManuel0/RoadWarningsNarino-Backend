package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    Optional<UserStatistics> findByUserId(Long userId);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.reputationPoints DESC")
    List<UserStatistics> findTopByReputationPoints(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.alertsCreated DESC")
    List<UserStatistics> findTopByAlertsCreated(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.upvotesReceived DESC")
    List<UserStatistics> findTopByUpvotesReceived(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us WHERE us.reputationPoints >= :minPoints")
    Page<UserStatistics> findByReputationPointsGreaterThanEqual(Integer minPoints, Pageable pageable);
}

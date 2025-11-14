package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.UserBadge;
import com.roadwarnings.narino.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long userId);

    List<UserBadge> findByBadgeType(BadgeType badgeType);

    Optional<UserBadge> findByUserIdAndBadgeType(Long userId, BadgeType badgeType);

    boolean existsByUserIdAndBadgeType(Long userId, BadgeType badgeType);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}

package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserId(Long userId);

    List<DeviceToken> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Optional<DeviceToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.token = :token")
    void deactivateToken(@Param("token") String token);

    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.user.id = :userId")
    void deactivateAllTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT dt.token FROM DeviceToken dt WHERE dt.user.id = :userId AND dt.isActive = true")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);
}

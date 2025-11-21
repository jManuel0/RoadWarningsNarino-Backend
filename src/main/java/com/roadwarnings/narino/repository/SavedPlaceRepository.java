package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.SavedPlace;
import com.roadwarnings.narino.enums.SavedPlaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    List<SavedPlace> findByUserId(Long userId);

    Optional<SavedPlace> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    boolean existsByUserIdAndType(Long userId, SavedPlaceType type);

    Optional<SavedPlace> findByUserIdAndType(Long userId, SavedPlaceType type);
}

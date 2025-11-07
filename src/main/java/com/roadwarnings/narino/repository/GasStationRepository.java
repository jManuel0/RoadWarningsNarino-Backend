package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.GasStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GasStationRepository extends JpaRepository<GasStation, Long> {
    List<GasStation> findByIsAvailable(Boolean isAvailable);
    List<GasStation> findByMunicipality(String municipality);
}
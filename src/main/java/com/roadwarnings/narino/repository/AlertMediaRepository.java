package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.AlertMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertMediaRepository extends JpaRepository<AlertMedia, Long> {

    List<AlertMedia> findByAlertOrderByPositionAsc(Alert alert);
}


package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.AlertReport;
import com.roadwarnings.narino.enums.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertReportRepository extends JpaRepository<AlertReport, Long> {

    List<AlertReport> findByAlertId(Long alertId);

    Page<AlertReport> findByAlertId(Long alertId, Pageable pageable);

    List<AlertReport> findByReporterId(Long reporterId);

    Page<AlertReport> findByReporterId(Long reporterId, Pageable pageable);

    List<AlertReport> findByReviewed(Boolean reviewed);

    Page<AlertReport> findByReviewed(Boolean reviewed, Pageable pageable);

    List<AlertReport> findByReason(ReportReason reason);

    @Query("SELECT COUNT(ar) FROM AlertReport ar WHERE ar.alert.id = :alertId")
    Long countByAlertId(@Param("alertId") Long alertId);

    @Query("SELECT COUNT(ar) FROM AlertReport ar WHERE ar.alert.id = :alertId AND ar.reviewed = false")
    Long countPendingByAlertId(@Param("alertId") Long alertId);

    boolean existsByAlertIdAndReporterId(Long alertId, Long reporterId);

    @Query("SELECT ar FROM AlertReport ar WHERE ar.alert.id = :alertId AND ar.reviewed = false")
    List<AlertReport> findPendingByAlertId(@Param("alertId") Long alertId);
}

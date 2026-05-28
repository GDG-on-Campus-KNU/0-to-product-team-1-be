package com.gdg.backend.repository;

import com.gdg.backend.entity.ReportWeekly;
import com.gdg.backend.entity.ReportWeeklyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportWeeklyRepository extends JpaRepository<ReportWeekly, ReportWeeklyId> {

    List<ReportWeekly> findAllByUserIdOrderByGeneratedAtDesc(Long userId);
}

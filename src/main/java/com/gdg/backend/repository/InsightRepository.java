package com.gdg.backend.repository;

import com.gdg.backend.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findAllByReport_ReportId(Long reportId);
}

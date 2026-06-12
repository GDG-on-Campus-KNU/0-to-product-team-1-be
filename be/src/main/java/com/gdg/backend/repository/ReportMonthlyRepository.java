package com.gdg.backend.repository;

import com.gdg.backend.entity.ReportMonthly;
import com.gdg.backend.entity.ReportMonthlyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportMonthlyRepository extends JpaRepository<ReportMonthly, ReportMonthlyId> {

    List<ReportMonthly> findAllByUserIdOrderByMonthIdDesc(Long userId);

    Optional<ReportMonthly> findByMonthIdAndUserId(String monthId, Long userId);
}

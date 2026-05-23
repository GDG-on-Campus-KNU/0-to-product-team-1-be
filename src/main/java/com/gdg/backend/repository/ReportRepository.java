package com.gdg.backend.repository;

import com.gdg.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);
}

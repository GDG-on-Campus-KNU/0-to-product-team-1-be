package com.gdg.backend.repository;

import com.gdg.backend.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightRepository extends JpaRepository<Insight, Long> {
}

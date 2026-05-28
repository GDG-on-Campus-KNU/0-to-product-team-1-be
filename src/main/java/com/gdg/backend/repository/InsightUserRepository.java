package com.gdg.backend.repository;

import com.gdg.backend.entity.InsightUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsightUserRepository extends JpaRepository<InsightUser, Long> {

    List<InsightUser> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);
}

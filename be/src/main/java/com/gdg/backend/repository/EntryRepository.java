package com.gdg.backend.repository;

import com.gdg.backend.entity.Entry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    Optional<Entry> findFirstByUser_UserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime start, LocalDateTime end);

    List<Entry> findAllByUser_UserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT e.drillId FROM Entry e WHERE e.user.userId = :userId AND e.drillId IS NOT NULL ORDER BY e.createdAt DESC")
    List<Integer> findRecentDrillIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByUser_UserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 일 1회 정책 — 오늘 이미 입력했는지 확인
    boolean existsByUser_UserIdAndRecordedDate(Long userId, LocalDate recordedDate);

    // 피드백 시 entryId 누락 대비 — userId + 날짜로 당일 기록 조회 (일 1회 정책상 최대 1건)
    Optional<Entry> findFirstByUser_UserIdAndRecordedDate(Long userId, LocalDate recordedDate);

    List<Entry> findByUser_UserIdAndRecordedDateBetween(Long userId, LocalDate start, LocalDate end);

    int countByUser_UserIdAndRecordedDateBetween(Long userId, LocalDate start, LocalDate end);

    int countByUser_UserIdAndRecordedDateBetweenAndDrillCompletedTrue(Long userId, LocalDate start, LocalDate end);

    // 10.4 90일 미사용 조회
    @Query("SELECT DISTINCT e.user.userId FROM Entry e WHERE e.createdAt >= :since")
    List<Long> findActiveUserIdsSince(@Param("since") LocalDateTime since);
}

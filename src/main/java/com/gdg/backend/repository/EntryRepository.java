package com.gdg.backend.repository;

import com.gdg.backend.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    Optional<Entry> findByUser_UserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}

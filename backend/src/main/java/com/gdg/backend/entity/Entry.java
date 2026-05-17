package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "self_condition")
    private Integer selfCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "llm_result", columnDefinition = "jsonb")
    private Map<String, Object> llmResult;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

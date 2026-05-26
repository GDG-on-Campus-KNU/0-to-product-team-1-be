package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pattern_analysis", columnDefinition = "jsonb")
    private Map<String, Object> patternAnalysis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_distribution", columnDefinition = "jsonb")
    private Map<String, Object> emotionDistribution;

    @Column(name = "week_of", length = 20)
    private String weekOf;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "report_type", length = 20)
    private String reportType;

    @Column(name = "period_id", length = 20)
    private String periodId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocks_json", columnDefinition = "jsonb")
    private Map<String, Object> blocksJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "visualizations_json", columnDefinition = "jsonb")
    private Map<String, Object> visualizationsJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

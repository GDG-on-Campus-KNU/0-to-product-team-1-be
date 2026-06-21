package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reports_weekly")
@IdClass(ReportWeeklyId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportWeekly {

    @Id
    @Column(name = "week_id", length = 10)
    private String weekId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocks_json", columnDefinition = "jsonb")
    private Map<String, Object> blocksJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "visualizations_json", columnDefinition = "jsonb")
    private Map<String, Object> visualizationsJson;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private boolean checked = false;

    @Column(name = "user_memo", columnDefinition = "TEXT")
    private String userMemo;

    public static ReportWeekly of(String weekId, Long userId, Map<String, Object> blocksJson, Map<String, Object> visualizationsJson, LocalDateTime generatedAt) {
        return ReportWeekly.builder()
                .weekId(weekId)
                .userId(userId)
                .blocksJson(blocksJson)
                .visualizationsJson(visualizationsJson)
                .generatedAt(generatedAt)
                .build();
    }
}

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
}

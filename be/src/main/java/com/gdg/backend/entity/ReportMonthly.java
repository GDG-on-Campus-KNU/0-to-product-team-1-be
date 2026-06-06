package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reports_monthly")
@IdClass(ReportMonthlyId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMonthly {

    @Id
    @Column(name = "month_id", length = 10)
    private String monthId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocks_json", columnDefinition = "jsonb")
    private Map<String, Object> blocksJson;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public static ReportMonthly of(String monthId, Long userId, Map<String, Object> blocksJson, LocalDateTime generatedAt) {
        return ReportMonthly.builder()
                .monthId(monthId)
                .userId(userId)
                .blocksJson(blocksJson)
                .generatedAt(generatedAt)
                .build();
    }
}

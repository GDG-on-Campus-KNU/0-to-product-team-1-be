package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "baselines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Baseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baseline_id")
    private Long baselineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patterns_avg", columnDefinition = "jsonb")
    private Map<String, Object> patternsAvg;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "behaviors_avg", columnDefinition = "jsonb")
    private Map<String, Object> behaviorsAvg;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rejected_drills", columnDefinition = "jsonb")
    private Map<String, Object> rejectedDrills;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

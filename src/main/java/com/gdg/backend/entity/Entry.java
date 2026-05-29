package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
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
    @Column(name = "entry_id")
    private Long entryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 200)
    private String text;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label_result_json", columnDefinition = "jsonb")
    private Map<String, Object> labelResultJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendation_json", columnDefinition = "jsonb")
    private Map<String, Object> recommendationJson;

    @Column(name = "drill_id")
    private Integer drillId;

    @Column(name = "drill_category", length = 50)
    private String drillCategory;

    @Column(name = "drill_calendar_color", length = 20)
    private String drillCalendarColor;

    @Column(name = "recorded_date")
    private LocalDate recordedDate;

    @Column(name = "awaiting_answer")
    private Boolean awaitingAnswer;

    @Column(name = "offered_category", length = 50)
    private String offeredCategory;

    @Column(name = "crisis_flag")
    private Boolean crisisFlag;

    @Column(name = "self_condition")
    private Integer selfCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_json", columnDefinition = "jsonb")
    private Map<String, Object> contextJson;

    @Column(name = "drill_completed")
    private Boolean drillCompleted;

    @Column(name = "helpful")
    private Boolean helpful;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static Entry of(User user, String text, LocalDate recordedDate) {
        return Entry.builder()
                .user(user)
                .text(text)
                .recordedDate(recordedDate)
                .build();
    }
}

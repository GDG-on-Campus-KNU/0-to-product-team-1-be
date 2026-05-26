package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.gdg.backend.dto.request.EntryCreateRequest;

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
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> context;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "llm_result", columnDefinition = "jsonb")
    private Map<String, Object> llmResult;

    @Column(name = "drill_id")
    private Integer drillId;

    @Column(name = "drill_complete")
    private Boolean drillComplete;

    private Boolean helpful;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day", length = 20)
    private TimeOfDay timeOfDay;

    public static Entry of(User user, EntryCreateRequest request) {
        return Entry.builder()
                .user(user)
                .text(request.getText())
                .context(request.getContext())
                .drillComplete(false)
                .timeOfDay(request.getTimeOfDay())
                .build();
    }
}

package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "quiz_answers",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_quiz_answers_user_week",
        columnNames = {"user_id", "week_of"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_of", nullable = false, length = 20)
    private String weekOf;

    @Column(columnDefinition = "TEXT")
    private String predicted;

    @Column(columnDefinition = "TEXT")
    private String correct;

    @Column
    private Double gap;

    @CreationTimestamp
    @Column(name = "answered_at", updatable = false)
    private LocalDateTime answeredAt;
}

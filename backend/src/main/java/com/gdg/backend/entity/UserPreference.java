package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ask_sleep")
    private Boolean askSleep;

    @Column(name = "ask_activity")
    private Boolean askActivity;

    @Column(name = "ask_meal")
    private Boolean askMeal;

    @Column(name = "ask_caffeine", columnDefinition = "boolean default false")
    private Boolean askCaffeine;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

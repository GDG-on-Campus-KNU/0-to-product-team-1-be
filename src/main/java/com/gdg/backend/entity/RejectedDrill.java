package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "rejected_drills",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_rejected_drills_user_drill",
        columnNames = {"user_id", "drill_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectedDrill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "drill_id", nullable = false)
    private Integer drillId;

    @CreationTimestamp
    @Column(name = "rejected_at", updatable = false)
    private LocalDateTime rejectedAt;
}

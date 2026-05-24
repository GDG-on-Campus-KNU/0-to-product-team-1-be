package com.gdg.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}

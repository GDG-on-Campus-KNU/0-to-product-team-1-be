package com.gdg.backend.service;

import com.gdg.backend.entity.BlacklistedToken;
import com.gdg.backend.repository.BlacklistedTokenRepository;
import com.gdg.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void blacklist(String token) {
        LocalDateTime expiresAt = jwtUtil.extractExpiration(token);
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiresAt(expiresAt)
                        .build()
        );
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *") // 매시 정각 만료 토큰 정리
    @Transactional
    public void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteAllExpired(LocalDateTime.now());
    }
}

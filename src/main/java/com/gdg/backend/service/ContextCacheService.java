package com.gdg.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 4.6 캐시 키: "ctx:{user_id}:{YYYY-MM-DD}" (KST 날짜)
 * 4.1 첫 입력 시 sleep_hours / social_today / exercise_today 저장 (self_condition 제외 — 4.3)
 * 4.2 이후 입력 시 누락 필드 자동 채움
 * 4.5 TTL = KST 자정까지 남은 초
 * 4.4 Redis 다운 시 인메모리 폴백
 * 4.7 Redis 선택 (docker-compose에 컨테이너 존재)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextCacheService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 4.4 Redis 다운 시 인메모리 폴백 맵 (단일 인스턴스 환경 가정)
    private final Map<String, Map<String, Object>> fallbackMap = new ConcurrentHashMap<>();

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 4.6 캐시 키 형식
    private String cacheKey(Long userId) {
        LocalDate today = LocalDate.now(KST);
        return "ctx:" + userId + ":" + today;
    }

    // 4.5 KST 자정까지 남은 Duration
    private Duration ttlUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(KST);
        return Duration.between(now, midnight);
    }

    /**
     * 4.1 첫 입력 또는 빈 필드가 있을 때 컨텍스트 저장.
     * 4.3 self_condition은 캐시에 저장하지 않음.
     */
    public void storeContext(Long userId, Map<String, Object> context) {
        Map<String, Object> toCache = new HashMap<>();
        if (context.containsKey("sleep_hours"))    toCache.put("sleep_hours",    context.get("sleep_hours"));
        if (context.containsKey("social_today"))   toCache.put("social_today",   context.get("social_today"));
        if (context.containsKey("exercise_today")) toCache.put("exercise_today", context.get("exercise_today"));

        if (toCache.isEmpty()) return;

        String key = cacheKey(userId);
        try {
            String json = objectMapper.writeValueAsString(toCache);
            redisTemplate.opsForValue().set(key, json, ttlUntilMidnight());
        } catch (Exception e) {
            log.warn("Redis store failed for key={}, using fallback. error={}", key, e.getMessage());
            fallbackMap.put(key, toCache);
        }
    }

    /**
     * 4.2 컨텍스트 조회 후 누락 필드 자동 채움.
     * 4.3 self_condition은 항상 사용자 입력 그대로 사용.
     * 반환값: 채워진 컨텍스트 (원본 수정 없이 새 Map 반환)
     */
    public Map<String, Object> fillContext(Long userId, Map<String, Object> incoming) {
        Map<String, Object> result = new HashMap<>(incoming);
        String key = cacheKey(userId);

        Map<String, Object> cached = loadFromCache(key);
        if (cached == null || cached.isEmpty()) {
            return result;
        }

        // 누락 필드만 채움 (self_condition은 캐시에서 안 가져옴 — 4.3)
        for (String field : new String[]{"sleep_hours", "social_today", "exercise_today"}) {
            if (!result.containsKey(field) || result.get(field) == null) {
                if (cached.containsKey(field)) {
                    result.put(field, cached.get(field));
                }
            }
        }
        return result;
    }

    // 4.4 Redis → fallback 순서로 조회
    private Map<String, Object> loadFromCache(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return fallbackMap.get(key);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Redis load failed for key={}, using fallback. error={}", key, e.getMessage());
            return fallbackMap.get(key);
        }
    }
}

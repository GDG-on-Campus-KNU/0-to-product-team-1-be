package com.gdg.backend.scheduler;

import com.gdg.backend.entity.Baseline;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.repository.BaselineRepository;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.UserRepository;
import com.gdg.backend.service.MlClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 14.16 매일 0:30 KST baseline 갱신 cron
 *
 * <p>ML은 주간 리포트 생성 시 baseline을 읽기만 하고(compare_to_baseline),
 * 갱신은 POST /baseline/recompute를 호출해야만 일어난다. 이 스케줄러가 매일
 * 사용자별 최근 30일 entries를 보내 갱신하고, 응답 스냅샷을 BE baselines
 * 테이블에도 보관한다(ML 컨테이너 재배포 시 ML 쪽 sqlite 유실 대비).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaselineUpdateScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int WINDOW_DAYS = 30;

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final BaselineRepository baselineRepository;
    private final MlClientService mlClientService;

    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul")
    public void updateBaselines() {
        log.info("Baseline 갱신 cron 시작");
        LocalDate end = LocalDate.now(KST);
        LocalDate start = end.minusDays(WINDOW_DAYS);

        userRepository.findAll().forEach(user -> {
            try {
                List<Entry> entries = entryRepository
                        .findByUser_UserIdAndRecordedDateBetween(user.getUserId(), start, end);

                if (entries.isEmpty()) {
                    log.debug("사용자 {} 최근 {}일 entries 없음, 스킵", user.getUserId(), WINDOW_DAYS);
                    return;
                }

                List<Map<String, Object>> entryData = entries.stream()
                        .map(e -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("created_at", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
                            m.put("self_condition", e.getContextJson() != null ? e.getContextJson().getOrDefault("self_condition", 0) : 0);
                            m.put("context", e.getContextJson() != null ? e.getContextJson() : Map.of());
                            m.put("label_result", e.getLabelResultJson() != null ? e.getLabelResultJson() : Map.of());
                            return m;
                        })
                        .toList();

                Map<String, Object> body = new HashMap<>();
                body.put("user_id", String.valueOf(user.getUserId()));
                body.put("entries", entryData);
                body.put("window_days", WINDOW_DAYS);
                // ML recompute는 rejected_drills 미전달 시 빈 배열로 덮어쓰므로 기존 값 보존
                body.put("rejected_drills", fetchExistingRejectedDrills(user.getUserId()));
                Map<String, Object> snapshot = mlClientService.recomputeBaseline(body);

                Baseline baseline = baselineRepository.findById(user.getUserId())
                        .orElseGet(() -> {
                            Baseline b = new Baseline();
                            b.setUser(user);
                            return b;
                        });
                baseline.setSnapshotJson(snapshot);
                baseline.setCapturedAt(LocalDateTime.now(KST));
                baselineRepository.save(baseline);

                log.info("사용자 {} baseline 갱신 완료 (entries {}개)", user.getUserId(), entries.size());
            } catch (Exception e) {
                log.error("사용자 {} baseline 갱신 실패", user.getUserId(), e);
            }
        });
        log.info("Baseline 갱신 cron 완료");
    }

    /** ML 기존 baseline의 rejected_drills 조회 — 미존재(404) 등 실패 시 빈 목록 */
    private List<Object> fetchExistingRejectedDrills(Long userId) {
        try {
            Map<String, Object> existing = mlClientService.getBaseline(String.valueOf(userId));
            if (existing != null && existing.get("rejected_drills") instanceof List<?> rejected) {
                return new java.util.ArrayList<>(rejected);
            }
        } catch (Exception e) {
            log.debug("사용자 {} 기존 baseline 없음 또는 조회 실패 — rejected_drills 빈 목록 사용", userId);
        }
        return List.of();
    }
}

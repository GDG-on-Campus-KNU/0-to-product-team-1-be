package com.gdg.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.backend.dto.ml.MlEntriesRequest;
import com.gdg.backend.dto.ml.MlEntriesResponse;
import com.gdg.backend.dto.ml.MlLabelRequest;
import com.gdg.backend.dto.ml.MlLabelResult;
import com.gdg.backend.dto.ml.MlRecommendRequest;
import com.gdg.backend.dto.ml.MlRecommendResponse;
import com.gdg.backend.dto.ml.MlAfterAskRequest;
import com.gdg.backend.dto.ml.MlReportRequest;
import com.gdg.backend.dto.ml.MlWeeklyRequest;
import com.gdg.backend.exception.MlUnavailableException;
import com.gdg.backend.exception.QuotaExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlClientService {

    private final WebClient mlWebClient;
    private final ObjectMapper objectMapper;

    @Value("${ml.service.timeout-label:10000}")
    private long labelTimeoutMs;

    @Value("${ml.service.timeout-recommend:3000}")
    private long recommendTimeoutMs;

    @Value("${ml.service.timeout-weekly:5000}")
    private long weeklyTimeoutMs;

    @Value("${ml.service.timeout-health:3000}")
    private long healthTimeoutMs;

    @Value("${ml.service.timeout:10000}")
    private long defaultTimeoutMs;

    /**
     * ML POST /entries 통합 호출 — 라벨링 + 추천 한 번에
     */
    public MlEntriesResponse callEntries(MlEntriesRequest request) {
        try {
            return mlWebClient.post()
                    .uri("/entries")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MlEntriesResponse.class)
                    .timeout(Duration.ofMillis(defaultTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException(parseRetryAfter(e.getResponseBodyAsString()));
            }
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new MlUnavailableException("entries");
            }
            log.error("ML /entries 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ML 서비스 호출 실패 (entries): " + e.getResponseBodyAsString(), e);
        } catch (QuotaExceededException | MlUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("ML /entries 호출 에러", e);
            throw new MlUnavailableException("connection");
        }
    }

    /**
     * ML /label 호출 — 텍스트 라벨링
     */
    public MlLabelResult label(String text, Long userId) {
        MlLabelRequest request = MlLabelRequest.builder()
                .text(text)
                .userId(String.valueOf(userId))
                .build();

        try {
            return mlWebClient.post()
                    .uri("/label")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MlLabelResult.class)
                    .timeout(Duration.ofMillis(labelTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException(parseRetryAfter(e.getResponseBodyAsString()));
            }
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new MlUnavailableException("label");
            }
            log.error("ML /label 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ML 서비스 호출 실패 (label): " + e.getResponseBodyAsString(), e);
        } catch (QuotaExceededException | MlUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("ML /label 호출 에러", e);
            throw new MlUnavailableException("connection");
        }
    }

    /**
     * ML /recommend 호출 — 드릴 추천 (5 type)
     */
    public MlRecommendResponse recommend(MlRecommendRequest request) {
        try {
            return mlWebClient.post()
                    .uri("/recommend")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MlRecommendResponse.class)
                    .timeout(Duration.ofMillis(recommendTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException(parseRetryAfter(e.getResponseBodyAsString()));
            }
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new MlUnavailableException("recommend");
            }
            log.error("ML /recommend 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ML 서비스 호출 실패 (recommend): " + e.getResponseBodyAsString(), e);
        } catch (QuotaExceededException | MlUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("ML /recommend 호출 에러", e);
            throw new MlUnavailableException("connection");
        }
    }

    /** 14.2 ask-first 후속 — ML POST /recommend/after_ask */
    public MlRecommendResponse callAfterAsk(MlAfterAskRequest request) {
        try {
            return mlWebClient.post()
                    .uri("/recommend/after_ask")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MlRecommendResponse.class)
                    .timeout(Duration.ofMillis(recommendTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException(parseRetryAfter(e.getResponseBodyAsString()));
            }
            throw new MlUnavailableException("after_ask");
        } catch (QuotaExceededException | MlUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new MlUnavailableException("after_ask");
        }
    }

    /** 14.5/14.6 드릴 단건/목록 — ML GET /drills/{id}, /drills */
    public java.util.Map<String, Object> getDrill(String drillId) {
        return mlWebClient.get().uri("/drills/{id}", drillId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    public java.util.List<java.util.Map<String, Object>> getDrills() {
        return mlWebClient.get().uri("/drills")
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 14.9 reports/pending */
    public java.util.Map<String, Object> getReportsPending(String userId) {
        return mlWebClient.get().uri(u -> u.path("/reports/pending").queryParam("user_id", userId).build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 14.10 reports/{id}/read */
    public void markReportRead(String reportId) {
        mlWebClient.patch().uri("/reports/{id}/read", reportId)
                .retrieve().toBodilessEntity()
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 14.11 weekly/quiz */
    public java.util.Map<String, Object> patchWeeklyQuiz(java.util.Map<String, Object> body) {
        return mlWebClient.patch().uri("/weekly/quiz")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 14.12/14.13 insights */
    public java.util.Map<String, Object> postInsights(java.util.Map<String, Object> body) {
        return mlWebClient.post().uri("/insights").bodyValue(body)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    public java.util.Map<String, Object> getInsights(String userId) {
        return mlWebClient.get().uri(u -> u.path("/insights").queryParam("user_id", userId).build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 14.14 export */
    public java.util.Map<String, Object> exportUserData(String userId) {
        return mlWebClient.get().uri(u -> u.path("/export").queryParam("user_id", userId).build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 드릴 피드백 — ML POST /feedback */
    public java.util.Map<String, Object> postFeedback(java.util.Map<String, Object> body) {
        return mlWebClient.post().uri("/feedback").bodyValue(body)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .timeout(Duration.ofMillis(defaultTimeoutMs)).block();
    }

    /** 7.2 주간 리포트 생성 — ML POST /reports */
    public java.util.Map<String, Object> callReports(MlReportRequest request) {
        try {
            return mlWebClient.post()
                    .uri("/reports")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                    .timeout(Duration.ofMillis(weeklyTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("ML /reports 호출 실패: status={}", e.getStatusCode());
            throw new RuntimeException("ML /reports 호출 실패", e);
        } catch (Exception e) {
            log.error("ML /reports 호출 에러", e);
            throw new RuntimeException("ML 서비스에 연결할 수 없습니다.", e);
        }
    }

    /** 7.2 주간 분석 — ML POST /weekly (7.6 time_of_day 포함) */
    public java.util.Map<String, Object> callWeekly(MlWeeklyRequest request) {
        try {
            return mlWebClient.post()
                    .uri("/weekly")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                    .timeout(Duration.ofMillis(weeklyTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("ML /weekly 호출 실패: status={}", e.getStatusCode());
            throw new RuntimeException("ML /weekly 호출 실패", e);
        } catch (Exception e) {
            log.error("ML /weekly 호출 에러", e);
            throw new RuntimeException("ML 서비스에 연결할 수 없습니다.", e);
        }
    }

    /** 10.1 사용자 데이터 삭제 — ML DELETE /users/{id}/data */
    public void deleteUserData(String userId) {
        try {
            mlWebClient.delete()
                    .uri("/users/{userId}/data", userId)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(defaultTimeoutMs))
                    .block();
        } catch (Exception e) {
            log.error("ML 사용자 데이터 삭제 실패: userId={}", userId, e);
            throw new RuntimeException("ML 사용자 데이터 삭제 실패", e);
        }
    }

    /**
     * 5.4 시연 전 quota 리셋 — ML POST /admin/quota/reset 포워딩
     */
    public void resetQuota(String userId, String adminToken) {
        // ML expects plain string body (user_id) or "all" for global reset
        String body = (userId != null && !userId.isBlank()) ? userId : "all";
        mlWebClient.post()
                .uri("/admin/quota/reset")
                .header("X-Admin-Token", adminToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue("\"" + body + "\"")
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(defaultTimeoutMs))
                .block();
    }

    // ML 429 응답 body에서 retry_after_seconds 추출
    private long parseRetryAfter(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode detail = root.path("detail");
            if (!detail.isMissingNode()) {
                JsonNode seconds = detail.path("retry_after_seconds");
                if (!seconds.isMissingNode()) return seconds.asLong(60);
            }
            JsonNode direct = root.path("retry_after_seconds");
            if (!direct.isMissingNode()) return direct.asLong(60);
        } catch (Exception e) {
            log.warn("retry_after_seconds 파싱 실패, 기본값 60 사용. body={}", body);
        }
        return 60;
    }
}

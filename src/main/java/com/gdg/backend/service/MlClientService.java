package com.gdg.backend.service;

import com.gdg.backend.dto.ml.MlLabelRequest;
import com.gdg.backend.dto.ml.MlLabelResult;
import com.gdg.backend.dto.ml.MlRecommendRequest;
import com.gdg.backend.dto.ml.MlRecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlClientService {

    private final WebClient mlWebClient;

    @Value("${ml.service.timeout:10000}")
    private long timeoutMs;

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
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("ML /label 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ML 서비스 호출 실패 (label): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("ML /label 호출 에러", e);
            throw new RuntimeException("ML 서비스에 연결할 수 없습니다.", e);
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
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("ML /recommend 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ML 서비스 호출 실패 (recommend): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("ML /recommend 호출 에러", e);
            throw new RuntimeException("ML 서비스에 연결할 수 없습니다.", e);
        }
    }
}

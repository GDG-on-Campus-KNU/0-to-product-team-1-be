# 주간 리포트 visualizationsJson 필드 누락 원인 및 수정

## 문제

`GET /reports/weekly/{weekId}` 응답의 `visualizationsJson`에 아래 3개 필드가 없었음:
- `discoveries` (이번 주의 발견)
- `pattern_diff` (패턴 변화)
- `emotion_pentagon` (주간 감정 분포)

대신 이런 구조만 들어있었음:
```json
{
  "visualizationsJson": {
    "emotion_trend": [...],
    "category_distribution": {...}
  }
}
```

## 원인

BE → ML 전송 시 필드명 불일치 3개:

| 위치 | 버그 (BE가 보낸 값) | 정상 (ML이 기대하는 값) |
|------|-------------------|----------------------|
| `MlWeeklyRequest.java` | `"week_id"` | `"week"` |
| `WeeklyReportScheduler.java` entry 맵 | `"context_json"` | `"context"` |
| `WeeklyReportScheduler.java` entry 맵 | `"label_result_json"` | `"label_result"` |

ML `POST /weekly`는 `week` 파라미터가 없으면 기본값(데모 데이터)으로 처리하고,
`context` / `label_result` 키가 없으면 패턴/감정 분석이 비어서
`emotion_pentagon`, `pattern_diff`, `discoveries`가 빈 값 또는 누락됨.

## 수정 내용

### 1. `be/src/main/java/com/gdg/backend/dto/ml/MlWeeklyRequest.java`
```java
// 전
@JsonProperty("week_id")
private String weekId;

// 후
@JsonProperty("week")
private String weekId;
```

### 2. `be/src/main/java/com/gdg/backend/scheduler/WeeklyReportScheduler.java`
```java
// 전
m.put("context_json", e.getContextJson() != null ? e.getContextJson() : Map.of());
m.put("label_result_json", e.getLabelResultJson() != null ? e.getLabelResultJson() : Map.of());

// 후
m.put("context", e.getContextJson() != null ? e.getContextJson() : Map.of());
m.put("label_result", e.getLabelResultJson() != null ? e.getLabelResultJson() : Map.of());
```

### 3. `be/src/main/java/com/gdg/backend/dto/request/DiscoveriesRequest.java` (신규)
`POST /discoveries` 요청 바디가 Swagger에서 `additionalProp1/2/3`으로 보이던 문제 수정.
`Map<String, Object>` → 명시적 DTO로 교체:
```json
{ "discoveries": ["string1", "string2"] }
```

## 수정 후 예상 응답 (visualizationsJson)

ML이 정상 데이터 받으면 아래 필드들이 채워짐:
```json
{
  "emotion_pentagon": {
    "axes": [
      {"label": "불안", "value": 0.3},
      ...
    ],
    "dominant": "불안"
  },
  "pattern_diff": [
    {"pattern": "미래예측", "current_percent": 75, "prev_percent": 60, "delta_percent": 15, "arrow": "up"}
  ],
  "discoveries": [
    {"text": "잠이 부족한 날 불안이 더 자주 보였어요", "category": "context", "source": "system"}
  ],
  "condition_flow": {...},
  "weekly_coaching": {...}
}
```

package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MlWeeklyRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("week")
    private String weekId;

    @JsonProperty("entry_count")
    private int entryCount;

    @JsonProperty("entries")
    private List<Map<String, Object>> entries;

    @JsonProperty("prev_entries")
    private List<Map<String, Object>> prevEntries;
}

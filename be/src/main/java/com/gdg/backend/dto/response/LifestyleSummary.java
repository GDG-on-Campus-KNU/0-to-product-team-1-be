package com.gdg.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LifestyleSummary {
    private double avgSleepHours;
    private double avgExerciseMinutes;
    private double avgCondition;
    private String socialMode;
    private Double prevWeekSleepHours;
    private Double prevWeekExerciseMinutes;
    private Double prevWeekCondition;
    private String prevWeekSocialMode;
}

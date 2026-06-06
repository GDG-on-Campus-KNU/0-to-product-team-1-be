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

    public static LifestyleSummary of(double avgSleepHours, double avgExerciseMinutes, double avgCondition, String socialMode,
                                      Double prevWeekSleepHours, Double prevWeekExerciseMinutes, Double prevWeekCondition, String prevWeekSocialMode) {
        return LifestyleSummary.builder()
                .avgSleepHours(avgSleepHours)
                .avgExerciseMinutes(avgExerciseMinutes)
                .avgCondition(avgCondition)
                .socialMode(socialMode)
                .prevWeekSleepHours(prevWeekSleepHours)
                .prevWeekExerciseMinutes(prevWeekExerciseMinutes)
                .prevWeekCondition(prevWeekCondition)
                .prevWeekSocialMode(prevWeekSocialMode)
                .build();
    }
}

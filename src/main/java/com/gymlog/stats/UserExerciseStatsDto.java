package com.gymlog.stats;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserExerciseStatsDto {

    private Long id;
    private Long userId;
    private Long exerciseId;
    private String exerciseName;    // useful to display
    private String category;        // muscle group — useful for grouping
    private Integer totalSets;
    private Integer totalReps;
    private BigDecimal totalVolume;
    private BigDecimal maxWeight;
    private BigDecimal avgRepsLast5;
    private LocalDateTime lastPerformedAt;
}
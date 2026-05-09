package com.gymlog.set;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class WorkoutSetDto {
    private Long id;
    private Long workoutId;
    private Long exerciseId;
    private String exerciseName;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private String notes;
}
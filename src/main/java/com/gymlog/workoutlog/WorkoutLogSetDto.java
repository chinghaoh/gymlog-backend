package com.gymlog.workoutlog;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class WorkoutLogSetDto {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private LocalDate logDate;
}
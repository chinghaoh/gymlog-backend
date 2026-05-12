package com.gymlog.workoutlog;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkoutLogDto {
    private Long id;
    private Long userId;
    private Long workoutId;
    private String workoutName;
    private String splitCategory;
    private LocalDate date;
    private Integer energyLevel;
    private Integer durationMinutes;
    private String notes;
    private LocalDateTime createdAt;

}
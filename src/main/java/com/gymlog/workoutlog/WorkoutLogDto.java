package com.gymlog.workoutlog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkoutLogDto {
    private Long id;
    private Long userId;

    @NotNull(message = "Workout is required")
    private Long workoutId;
    private String workoutName;
    private String splitCategory;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Min(value = 1, message = "Energy level must be between 1 and 10")
    @Max(value = 10, message = "Energy level must be between 1 and 10")
    private Integer energyLevel;

    private Integer durationMinutes;
    private String notes;
    private LocalDateTime createdAt;

}
package com.gymlog.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkoutDto {
    private Long id;
    private Long userId;
    private String userName;

    @NotBlank(message = "Workout name is required")
    private String name;

    @NotNull(message = "Split category is required")
    private SplitCategory splitCategory;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    private String notes;
    private Integer totalSets;
    private LocalDateTime createdAt;
    private Boolean aiGenerated;
}
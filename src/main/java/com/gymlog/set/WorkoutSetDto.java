package com.gymlog.set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkoutSetDto {
    private Long id;
    private Long workoutId;
    private Long exerciseId;
    private String exerciseName;

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    private Integer setNumber;

    @NotNull(message = "Reps is required")
    @Min(value = 0, message = "Reps cannot be negative")
    private Integer reps;

    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight cannot be negative")
    private BigDecimal weight;

    private String notes;
    private LocalDateTime workoutCreatedAt;
}
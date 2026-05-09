package com.gymlog.record;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PersonalRecordDto {
    private Long id;
    private Long userId;
    private Long exerciseId;
    private String exerciseName;
    private String category;
    private Long workoutId;
    private BigDecimal weight;
    private Integer reps;
    private LocalDateTime achievedAt;
}
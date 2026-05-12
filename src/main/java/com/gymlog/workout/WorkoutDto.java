package com.gymlog.workout;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkoutDto {
    private Long id;
    private Long userId;
    private String userName;
    private String name;
    private SplitCategory splitCategory;
    private Integer durationMinutes;
    private String notes;
    private LocalDateTime createdAt;
}
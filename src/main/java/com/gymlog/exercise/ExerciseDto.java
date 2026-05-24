package com.gymlog.exercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseDto {

    private Long id;
    private String name;
    private String category;
    private String equipment;
    private String difficulty;
    private String exerciseType;
    private String description;
    private String targetMuscle;
    private String secondaryMuscles;
    private String gifUrl;
    private Boolean isSeeded;
    private Boolean isActive;
    private Boolean aiCreated;
    private String instructions;

}
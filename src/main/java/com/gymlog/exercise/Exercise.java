package com.gymlog.exercise;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 250)
    private String category;

    @Column(nullable = false, length = 150)
    private String equipment;

    @Column(nullable = false, length = 50)
    private String difficulty;

    @Column(nullable = false, length = 150)
    private String exerciseType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String targetMuscle;

    @Column(columnDefinition = "TEXT")
    private String secondaryMuscles;

    @Column(length = 500)
    private String gifUrl;

    @Column(nullable = false)
    private Boolean isSeeded = false;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean aiCreated = false;
}

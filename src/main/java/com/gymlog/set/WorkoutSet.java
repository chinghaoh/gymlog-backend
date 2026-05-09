package com.gymlog.set;

import com.gymlog.exercise.Exercise;
import com.gymlog.workout.Workout;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;


@Entity
@Table(name = "workout_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer setNumber;

    @Column(nullable = false)
    private Integer reps;

    @Column(nullable = false)
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
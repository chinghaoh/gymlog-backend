package com.gymlog.stats;

import com.gymlog.exercise.Exercise;
import com.gymlog.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="user_exercise_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserExerciseStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="exercise_id")
    private Exercise exercise;

    @Column(nullable = false)
    private Integer totalSets = 0;

    @Column(nullable = false)
    private Integer totalReps = 0;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalVolume = BigDecimal.ZERO;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal maxWeight = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal avgRepsLast5;

    @Column
    private LocalDateTime lastPerformedAt;


}

package com.gymlog.record;

import com.gymlog.exercise.Exercise;
import com.gymlog.user.User;
import com.gymlog.workout.Workout;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "workout_log_id", nullable = false)
    private Workout workout;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weight;

    // the reps at that weight
    @Column(nullable = false)
    private Integer reps;

    // when it was achieved
    @Column(nullable = false)
    private LocalDateTime achievedAt;

    @PrePersist
    protected void onCreate() {
        achievedAt = LocalDateTime.now();
    }
}

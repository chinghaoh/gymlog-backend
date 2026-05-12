package com.gymlog.set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    List<WorkoutSet> findByWorkoutId(Long workoutId);

    List<WorkoutSet> findByWorkoutIdOrderBySetNumberAsc(Long workoutId);

    @Query("SELECT ws FROM WorkoutSet ws " +
            "WHERE ws.workout.user.id = :userId " +
            "AND ws.exercise.id = :exerciseId " +
            "ORDER BY ws.id DESC")
    List<WorkoutSet> findByUserIdAndExerciseId(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId);

    @Query("SELECT ws FROM WorkoutSet ws " +
            "WHERE ws.workout.user.id = :userId " +
            "AND ws.exercise.id = :exerciseId " +
            "ORDER BY ws.weight DESC, ws.reps DESC")
    List<WorkoutSet> findTopSetsByUserAndExercise(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId);
}
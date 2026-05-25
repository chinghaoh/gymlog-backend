package com.gymlog.workoutlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutLogSetRepository extends JpaRepository<WorkoutLogSet, Long> {

    @Query("SELECT wls FROM WorkoutLogSet wls " +
            "JOIN wls.workoutLog wl " +
            "WHERE wl.user.id = :userId " +
            "AND wls.exercise.id = :exerciseId " +
            "ORDER BY wl.date ASC")
    List<WorkoutLogSet> findByUserIdAndExerciseIdOrderByDate(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId);

    @Query("SELECT wls FROM WorkoutLogSet wls " +
            "JOIN wls.workoutLog wl " +
            "WHERE wl.user.id = :userId " +
            "AND wls.exercise.id = :exerciseId " +
            "ORDER BY wl.date DESC")
    List<WorkoutLogSet> findByUserIdAndExerciseIdOrderByDateDesc(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId);


    @Query("SELECT wls FROM WorkoutLogSet wls " +
            "JOIN wls.workoutLog wl " +
            "WHERE wl.id = :logId " +
            "ORDER BY wls.exercise.name ASC, wls.setNumber ASC")
    List<WorkoutLogSet> findByWorkoutLogId(@Param("logId") Long logId);
}
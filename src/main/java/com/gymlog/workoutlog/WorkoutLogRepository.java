package com.gymlog.workoutlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findByUserIdOrderByDateDesc(Long userId);
    List<WorkoutLog> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);
}
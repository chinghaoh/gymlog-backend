package com.gymlog.workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout,Long> {

    List<Workout> findByUserId(Long userId);

    List<Workout> findByName(String name);

    List<Workout> findBySplitCategory(SplitCategory splitCategory);

    List<Workout> findByUserIdOrderByDateDesc(Long userId);

    List<Workout> findTop5ByUserIdAndSplitCategoryOrderByDateDesc(
            Long userId, SplitCategory splitCategory);

    List<Workout> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

}

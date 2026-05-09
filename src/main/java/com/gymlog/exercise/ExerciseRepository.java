package com.gymlog.exercise;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise,Long>,
        JpaSpecificationExecutor<Exercise> {

    List<Exercise> findByIsActiveTrue();

    List<Exercise> findByIsActiveTrueAndCategoryIgnoreCase(String category);

    List<Exercise> findByIsActiveTrueAndEquipmentIgnoreCase(String equipment);

    List<Exercise> findByIsActiveTrueAndDifficultyIgnoreCase(String difficulty);

    List<Exercise> findByIsActiveTrueAndExerciseTypeIgnoreCase(String exerciseType);

    List<Exercise> findByIsActiveTrueAndNameContainingIgnoreCase(String name);

    boolean existsByIsSeededTrue();

    boolean existsByNameIgnoreCase(String name);


}

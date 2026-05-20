package com.gymlog.exercise;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise,Long>{

    boolean existsByIsSeededTrue();

    boolean existsByNameIgnoreCase(String name);

    @Query(value = "SELECT * FROM exercises WHERE " +
            "is_active = true AND " +
            "(:category IS NULL OR category = :category) AND " +
            "(:equipment IS NULL OR equipment = :equipment) AND " +
            "(:difficulty IS NULL OR difficulty = :difficulty) AND " +
            "(:name IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%')))",
            nativeQuery = true)
    List<Exercise> findByFilters(
            @Param("category") String category,
            @Param("equipment") String equipment,
            @Param("difficulty") String difficulty,
            @Param("name") String name
    );
    List<Exercise> findByCategoryInAndIsActiveTrue(List<String> categories);
}

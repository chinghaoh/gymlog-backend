package com.gymlog.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserExerciseStatsRepository extends JpaRepository <UserExerciseStats,Long> {

    Optional<UserExerciseStats> findByUserIdAndExerciseId(
            Long userId, Long exerciseId);

    List<UserExerciseStats> findByUserIdOrderByLastPerformedAtDesc(Long userId);
}

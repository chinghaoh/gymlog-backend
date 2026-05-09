package com.gymlog.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, Long> {

    Optional<PersonalRecord> findByUserIdAndExerciseId(Long userId, Long exerciseId);

    List<PersonalRecord> findByUserIdOrderByAchievedAtDesc(Long userId);

    List<PersonalRecord> findByUserIdAndExerciseIdOrderByAchievedAtDesc(
            Long userId, Long exerciseId);
}

package com.gymlog.stats;

import com.gymlog.common.AppException;
import com.gymlog.workout.Workout;
import com.gymlog.set.WorkoutSet;
import com.gymlog.set.WorkoutSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserExerciseStatsService {

    private final UserExerciseStatsRepository statsRepository;
    private final WorkoutSetRepository workoutSetRepository;

    private UserExerciseStatsDto mapToDto(UserExerciseStats stats) {
        UserExerciseStatsDto dto = new UserExerciseStatsDto();
        dto.setId(stats.getId());
        dto.setUserId(stats.getUser().getId());
        dto.setExerciseId(stats.getExercise().getId());
        dto.setExerciseName(stats.getExercise().getName());
        dto.setCategory(stats.getExercise().getCategory());
        dto.setTotalSets(stats.getTotalSets());
        dto.setTotalReps(stats.getTotalReps());
        dto.setTotalVolume(stats.getTotalVolume());
        dto.setMaxWeight(stats.getMaxWeight());
        dto.setAvgRepsLast5(stats.getAvgRepsLast5());
        dto.setLastPerformedAt(stats.getLastPerformedAt());
        return dto;
    }

    @Transactional
    public void updateStats(WorkoutSet savedSet) {

        Long userId = savedSet.getWorkout().getUser().getId();
        Long exerciseId = savedSet.getExercise().getId();

        UserExerciseStats stats = statsRepository
                .findByUserIdAndExerciseId(userId, exerciseId)
                .orElseGet(() -> createNewStats(savedSet));

        List<WorkoutSet> allSets = workoutSetRepository
                .findByUserIdAndExerciseId(userId, exerciseId);

        stats.setTotalSets(allSets.size());

        int totalReps = allSets.stream()
                .mapToInt(WorkoutSet::getReps)
                .sum();
        stats.setTotalReps(totalReps);

        BigDecimal totalVolume = allSets.stream()
                .map(s -> s.getWeight()
                        .multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalVolume(totalVolume);

        BigDecimal maxWeight = allSets.stream()
                .map(WorkoutSet::getWeight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        stats.setMaxWeight(maxWeight);

        BigDecimal avgRepsLast5 = calculateAvgRepsLast5(allSets);
        stats.setAvgRepsLast5(avgRepsLast5);

        stats.setLastPerformedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    @Transactional
    public void recalculateAfterDelete(Long userId, Long exerciseId) {

        List<WorkoutSet> remainingSets = workoutSetRepository
                .findByUserIdAndExerciseId(userId, exerciseId);

        Optional<UserExerciseStats> existingStats = statsRepository
                .findByUserIdAndExerciseId(userId, exerciseId);

        if (remainingSets.isEmpty()) {
            existingStats.ifPresent(statsRepository::delete);
            return;
        }

        UserExerciseStats stats = existingStats.orElseThrow();

        stats.setTotalSets(remainingSets.size());

        stats.setTotalReps(remainingSets.stream()
                .mapToInt(WorkoutSet::getReps)
                .sum());

        stats.setTotalVolume(remainingSets.stream()
                .map(s -> s.getWeight()
                        .multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        stats.setMaxWeight(remainingSets.stream()
                .map(WorkoutSet::getWeight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));

        stats.setAvgRepsLast5(calculateAvgRepsLast5(remainingSets));
        stats.setLastPerformedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    private BigDecimal calculateAvgRepsLast5(List<WorkoutSet> allSets) {


        List<WorkoutSet> last5Sessions = allSets.stream()
                .sorted((a, b) -> b.getWorkout().getDate()
                        .compareTo(a.getWorkout().getDate()))
                .collect(Collectors.toList());

        List<Long> last5WorkoutIds = last5Sessions.stream()
                .map(s -> s.getWorkout().getId())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        if (last5WorkoutIds.isEmpty()) return null;

        // filter sets to only those in last 5 workouts
        List<WorkoutSet> last5Sets = last5Sessions.stream()
                .filter(s -> last5WorkoutIds.contains(s.getWorkout().getId()))
                .collect(Collectors.toList());

        // average reps across those sets
        double avg = last5Sets.stream()
                .mapToInt(WorkoutSet::getReps)
                .average()
                .orElse(0);

        return BigDecimal.valueOf(avg)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private UserExerciseStats createNewStats(WorkoutSet set) {
        return UserExerciseStats.builder()
                .user(set.getWorkout().getUser())
                .exercise(set.getExercise())
                .totalSets(0)
                .totalReps(0)
                .totalVolume(BigDecimal.ZERO)
                .maxWeight(BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserExerciseStatsDto> getStatsByUserId(Long userId) {
        return statsRepository
                .findByUserIdOrderByLastPerformedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserExerciseStatsDto getStatsByUserAndExercise(
            Long userId, Long exerciseId) {
        return statsRepository
                .findByUserIdAndExerciseId(userId, exerciseId)
                .map(this::mapToDto)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "No stats found for user " + userId
                        + " and exercise " + exerciseId));
    }
}
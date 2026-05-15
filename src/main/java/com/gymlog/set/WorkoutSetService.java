package com.gymlog.set;

import com.gymlog.common.AppException;
import com.gymlog.exercise.Exercise;
import com.gymlog.exercise.ExerciseRepository;
import com.gymlog.record.PersonalRecordService;
import com.gymlog.stats.UserExerciseStatsService;
import com.gymlog.workout.Workout;
import com.gymlog.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutSetService {

    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalRecordService personalRecordService;
    private final UserExerciseStatsService statsService;

    private WorkoutSetDto mapToDto(WorkoutSet set) {
        WorkoutSetDto dto = new WorkoutSetDto();
        dto.setId(set.getId());
        dto.setWorkoutId(set.getWorkout().getId());
        dto.setExerciseId(set.getExercise().getId());
        dto.setExerciseName(set.getExercise().getName());
        dto.setSetNumber(set.getSetNumber());
        dto.setReps(set.getReps());
        dto.setWeight(set.getWeight());
        dto.setNotes(set.getNotes());
        dto.setWorkoutCreatedAt(set.getWorkout().getCreatedAt());
        return dto;
    }



    @Transactional(readOnly = true)
    public List<WorkoutSetDto> getSetsByWorkoutId(Long workoutId) {
        return workoutSetRepository.findByWorkoutIdOrderBySetNumberAsc(workoutId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutSetDto addSet(Long workoutId, Long exerciseId, WorkoutSetDto dto) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "Workout not found with id: " + workoutId));

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "Exercise not found with id: " + exerciseId));

        WorkoutSet workoutSet = WorkoutSet.builder()
                .workout(workout)
                .exercise(exercise)
                .setNumber(dto.getSetNumber())
                .reps(dto.getReps())
                .weight(dto.getWeight())
                .notes(dto.getNotes())
                .build();

        WorkoutSet saved = workoutSetRepository.save(workoutSet);

        personalRecordService.checkForPersonalRecord(saved);

        statsService.updateStats(saved);

        return mapToDto(saved);
    }

    @Transactional
    public WorkoutSetDto updateSet(Long id, WorkoutSetDto dto) {
        WorkoutSet existing = workoutSetRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "WorkoutSet not found with id: " + id));
        existing.setSetNumber(dto.getSetNumber());
        existing.setReps(dto.getReps());
        existing.setWeight(dto.getWeight());
        existing.setNotes(dto.getNotes());

        WorkoutSet saved = workoutSetRepository.save(existing);
        personalRecordService.checkForPersonalRecord(saved);
        statsService.updateStats(saved);

        return mapToDto(saved);

    }

    @Transactional
    public void deleteSet(Long id) {
        WorkoutSet existing = workoutSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSet not found"));

        Long userId = existing.getWorkout().getUser().getId();
        Long exerciseId = existing.getExercise().getId();

        workoutSetRepository.deleteById(id);

        statsService.recalculateAfterDelete(userId, exerciseId);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetDto> getSetsByUserAndExercise(Long userId, Long exerciseId) {
        return workoutSetRepository.findByUserIdAndExerciseIdOrderByDate(userId, exerciseId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
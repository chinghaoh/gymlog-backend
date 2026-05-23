package com.gymlog.workoutlog;

import com.gymlog.common.AppException;
import com.gymlog.record.PersonalRecordService;
import com.gymlog.set.WorkoutSetRepository;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import com.gymlog.workout.Workout;
import com.gymlog.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.gymlog.set.WorkoutSet;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;


@Service
@RequiredArgsConstructor
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final PersonalRecordService personalRecordService;


    @Transactional(readOnly = true)
    public List<WorkoutLogDto> getLogsByUserId(Long userId) {
        return workoutLogRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto> getLogsByDateRange(Long userId, LocalDate start, LocalDate end) {
        return workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutLogDto createLog(Long userId, WorkoutLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        Workout workout = workoutRepository.findById(dto.getWorkoutId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "Workout not found with id: " + dto.getWorkoutId()));

        WorkoutLog log = WorkoutLog.builder()
                .user(user)
                .workout(workout)
                .date(dto.getDate())
                .energyLevel(dto.getEnergyLevel())
                .notes(dto.getNotes())
                .build();

        WorkoutLog saved = workoutLogRepository.save(log);

        List<WorkoutSet> sets = workoutSetRepository.findByWorkoutIdOrdered(workout.getId());

        Map<Long, Optional<WorkoutSet>> highestPerExercise = sets.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getExercise().getId(),
                        Collectors.maxBy(Comparator.comparing(WorkoutSet::getWeight))
                ));

        for (Optional<WorkoutSet> optSet : highestPerExercise.values()) {
            optSet.ifPresent(personalRecordService::checkForPersonalRecord);
        }

        return mapToDto(saved);
    }

    @Transactional
    public void deleteLog(Long id) {
        workoutLogRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "Log not found with id: " + id));
        workoutLogRepository.deleteById(id);
    }

    private WorkoutLogDto mapToDto(WorkoutLog log) {
        WorkoutLogDto dto = new WorkoutLogDto();
        dto.setId(log.getId());
        dto.setUserId(log.getUser().getId());
        dto.setWorkoutId(log.getWorkout().getId());
        dto.setWorkoutName(log.getWorkout().getName());
        dto.setSplitCategory(log.getWorkout().getSplitCategory().name());
        dto.setDate(log.getDate());
        dto.setDurationMinutes(log.getWorkout().getDurationMinutes());
        dto.setEnergyLevel(log.getEnergyLevel());
        dto.setNotes(log.getNotes());
        dto.setCreatedAt(log.getCreatedAt());

        return dto;
    }
}
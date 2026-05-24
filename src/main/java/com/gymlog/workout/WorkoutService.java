package com.gymlog.workout;

import com.gymlog.common.AppException;
import com.gymlog.set.WorkoutSetRepository;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutSetRepository workoutSetRepository;

    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkoutsByUserId(Long userId) {
        return workoutRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutDto getWorkoutById(Long id) {
        return mapToDto(findWorkoutOrThrow(id));
    }

    @Transactional
    public WorkoutDto createWorkout(Long userId, WorkoutDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        Workout workout = Workout.builder()
                .user(user)
                .name(dto.getName())
                .splitCategory(dto.getSplitCategory())
                .durationMinutes(dto.getDurationMinutes())
                .notes(dto.getNotes())
                .aiGenerated(false)
                .build();

        return mapToDto(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutDto updateWorkout(Long id, WorkoutDto dto) {
        Workout existing = findWorkoutOrThrow(id);
        existing.setName(dto.getName());
        existing.setSplitCategory(dto.getSplitCategory());
        existing.setDurationMinutes(dto.getDurationMinutes());
        existing.setNotes(dto.getNotes());
        return mapToDto(workoutRepository.save(existing));
    }

    @Transactional
    public void deleteWorkout(Long id) {
        findWorkoutOrThrow(id);
        workoutRepository.deleteById(id);
    }

    private Workout findWorkoutOrThrow(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "Workout not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkoutsByRange(Long userId, String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return workoutRepository.findByUserIdAndCreatedAtBetween(
                        userId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateDuration(Long id, Integer durationMinutes) {
        Workout workout = findWorkoutOrThrow(id);
        workout.setDurationMinutes(durationMinutes);
        workoutRepository.save(workout);
    }

    private WorkoutDto mapToDto(Workout workout) {
        WorkoutDto dto = new WorkoutDto();
        dto.setId(workout.getId());
        dto.setUserId(workout.getUser().getId());
        dto.setUserName(workout.getUser().getName());
        dto.setName(workout.getName());
        dto.setSplitCategory(workout.getSplitCategory());
        dto.setDurationMinutes(workout.getDurationMinutes());
        dto.setNotes(workout.getNotes());
        dto.setCreatedAt(workout.getCreatedAt());
        dto.setTotalSets(workoutSetRepository.countByWorkoutId(workout.getId()));
        dto.setAiGenerated(workout.getAiGenerated());
        return dto;
    }
}
package com.gymlog.workout;

import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import lombok.RequiredArgsConstructor;
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


    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkoutsByUserId(Long userId) {
        return workoutRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutDto getWorkoutById(Long id) {
        return mapToDto(findWorkoutOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkoutsByDateRange(
            Long userId, LocalDate start, LocalDate end) {
        return workoutRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutDto createWorkout(Long userId, WorkoutDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Workout workout = Workout.builder()
                .user(user)
                .name(dto.getName())
                .splitCategory(dto.getSplitCategory())
                .date(dto.getDate())
                .durationMinutes(dto.getDurationMinutes())
                .energyLevel(dto.getEnergyLevel())
                .notes(dto.getNotes())
                .build();

        return mapToDto(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutDto updateWorkout(Long id, WorkoutDto dto) {
        Workout existing = findWorkoutOrThrow(id);
        existing.setName(dto.getName());
        existing.setSplitCategory(dto.getSplitCategory());
        existing.setDate(dto.getDate());
        existing.setDurationMinutes(dto.getDurationMinutes());
        existing.setEnergyLevel(dto.getEnergyLevel());
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
                .orElseThrow(() -> new RuntimeException("Workout not found with id: " + id));
    }

    private WorkoutDto mapToDto(Workout workout) {
        WorkoutDto dto = new WorkoutDto();
        dto.setId(workout.getId());
        dto.setUserId(workout.getUser().getId());
        dto.setUserName(workout.getUser().getName());
        dto.setName(workout.getName());
        dto.setSplitCategory(workout.getSplitCategory());
        dto.setDate(workout.getDate());
        dto.setDurationMinutes(workout.getDurationMinutes());
        dto.setEnergyLevel(workout.getEnergyLevel());
        dto.setNotes(workout.getNotes());
        dto.setCreatedAt(workout.getCreatedAt());
        return dto;
    }
}
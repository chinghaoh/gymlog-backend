package com.gymlog.workoutlog;

import com.gymlog.common.AppException;
import com.gymlog.exercise.Exercise;
import com.gymlog.record.PersonalRecordService;
import com.gymlog.set.WorkoutSet;
import com.gymlog.set.WorkoutSetRepository;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import com.gymlog.workout.Workout;
import com.gymlog.workout.WorkoutRepository;
import com.gymlog.workout.SplitCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutLogServiceTest {

    @Mock private WorkoutLogRepository workoutLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkoutRepository workoutRepository;
    @Mock private WorkoutSetRepository workoutSetRepository;
    @Mock private PersonalRecordService personalRecordService;
    @Mock private WorkoutLogSetRepository workoutLogSetRepository;

    @InjectMocks
    private WorkoutLogService workoutLogService;

    private User user;
    private Workout workout;
    private WorkoutLogDto dto;
    private WorkoutLog savedLog;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");

        workout = new Workout();
        workout.setId(1L);
        workout.setName("Push Day");
        workout.setSplitCategory(SplitCategory.PUSH);
        workout.setDurationMinutes(60);
        workout.setUser(user);

        dto = new WorkoutLogDto();
        dto.setWorkoutId(1L);
        dto.setDate(LocalDate.of(2026, 5, 25));
        dto.setEnergyLevel(8);

        savedLog = WorkoutLog.builder()
                .user(user)
                .workout(workout)
                .date(dto.getDate())
                .energyLevel(dto.getEnergyLevel())
                .build();
        savedLog.setId(1L);
    }

    // ── createLog ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Creates log successfully with valid user and workout")
    void createLog_validInput_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of());

        WorkoutLogDto result = workoutLogService.createLog(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getWorkoutId()).isEqualTo(1L);
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(result.getEnergyLevel()).isEqualTo(8);
    }

    @Test
    @DisplayName("Throws AppException when user not found")
    void createLog_userNotFound_throwsAppException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutLogService.createLog(99L, dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Throws AppException when workout not found")
    void createLog_workoutNotFound_throwsAppException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutLogService.createLog(1L, dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Workout not found");
    }

    @Test
    @DisplayName("Snapshots WorkoutSets into WorkoutLogSets on log creation")
    void createLog_withSets_snapshotsLogSets() {
        WorkoutSet set = buildWorkoutSet(1L, new BigDecimal("100.0"), 8);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of(set));

        workoutLogService.createLog(1L, dto);

        verify(workoutLogSetRepository, times(1)).save(any(WorkoutLogSet.class));
    }

    @Test
    @DisplayName("Snapshots multiple sets into WorkoutLogSets")
    void createLog_withMultipleSets_snapshotsAll() {
        WorkoutSet set1 = buildWorkoutSet(1L, new BigDecimal("100.0"), 8);
        WorkoutSet set2 = buildWorkoutSet(2L, new BigDecimal("80.0"), 10);
        WorkoutSet set3 = buildWorkoutSet(3L, new BigDecimal("60.0"), 12);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of(set1, set2, set3));

        workoutLogService.createLog(1L, dto);

        verify(workoutLogSetRepository, times(3)).save(any(WorkoutLogSet.class));
    }

    @Test
    @DisplayName("PR detection fires for highest weight per exercise")
    void createLog_withSets_firesPrDetection() {
        WorkoutSet set = buildWorkoutSet(1L, new BigDecimal("100.0"), 8);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of(set));

        workoutLogService.createLog(1L, dto);

        verify(personalRecordService, times(1))
                .checkForPersonalRecord(any(WorkoutSet.class), eq(dto.getDate()));
    }

    @Test
    @DisplayName("PR detection picks highest weight when multiple sets for same exercise")
    void createLog_multipleSetsSameExercise_prDetectionUsesHighestWeight() {
        Exercise exercise = buildExercise(1L);

        // same exercise, different weights — 100kg is highest
        WorkoutSet set1 = buildWorkoutSetWithExercise(exercise, new BigDecimal("80.0"), 8);
        WorkoutSet set2 = buildWorkoutSetWithExercise(exercise, new BigDecimal("100.0"), 6);
        WorkoutSet set3 = buildWorkoutSetWithExercise(exercise, new BigDecimal("60.0"), 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of(set1, set2, set3));

        workoutLogService.createLog(1L, dto);

        // PR detection should only fire once (highest weight per exercise)
        verify(personalRecordService, times(1))
                .checkForPersonalRecord(argThat(s ->
                        s.getWeight().compareTo(new BigDecimal("100.0")) == 0
                ), eq(dto.getDate()));
    }

    @Test
    @DisplayName("No PR detection when workout has no sets")
    void createLog_noSets_noPrDetection() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(savedLog);
        when(workoutSetRepository.findByWorkoutIdOrdered(1L)).thenReturn(List.of());

        workoutLogService.createLog(1L, dto);

        verify(personalRecordService, never())
                .checkForPersonalRecord(any(), any());
    }

    // ── deleteLog ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deletes log successfully when it exists")
    void deleteLog_existingLog_deletesSuccessfully() {
        when(workoutLogRepository.findById(1L)).thenReturn(Optional.of(savedLog));

        assertThatNoException().isThrownBy(() -> workoutLogService.deleteLog(1L));

        verify(workoutLogRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Throws AppException when log not found on delete")
    void deleteLog_notFound_throwsAppException() {
        when(workoutLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutLogService.deleteLog(99L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Log not found");
    }

    // ── getLogsByUserId ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Returns mapped DTOs for user")
    void getLogsByUserId_returnsMappedDtos() {
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(savedLog));

        List<WorkoutLogDto> result = workoutLogService.getLogsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWorkoutId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Returns empty list when user has no logs")
    void getLogsByUserId_noLogs_returnsEmptyList() {
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of());

        List<WorkoutLogDto> result = workoutLogService.getLogsByUserId(1L);

        assertThat(result).isEmpty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Exercise buildExercise(Long id) {
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName("Bench Press");
        ex.setCategory("Chest");
        return ex;
    }

    private WorkoutSet buildWorkoutSet(Long id, BigDecimal weight, int reps) {
        WorkoutSet set = new WorkoutSet();
        set.setId(id);
        set.setWorkout(workout);
        set.setExercise(buildExercise(id));
        set.setWeight(weight);
        set.setReps(reps);
        set.setSetNumber(1);
        return set;
    }

    private WorkoutSet buildWorkoutSetWithExercise(Exercise exercise, BigDecimal weight, int reps) {
        WorkoutSet set = new WorkoutSet();
        set.setWorkout(workout);
        set.setExercise(exercise);
        set.setWeight(weight);
        set.setReps(reps);
        set.setSetNumber(1);
        return set;
    }
}
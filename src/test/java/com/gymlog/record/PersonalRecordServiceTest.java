package com.gymlog.record;

import com.gymlog.exercise.Exercise;
import com.gymlog.user.User;
import com.gymlog.workout.Workout;
import com.gymlog.set.WorkoutSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalRecordServiceTest {

    @Mock
    private PersonalRecordRepository personalRecordRepository;

    @InjectMocks
    private PersonalRecordService personalRecordService;

    private User user;
    private Exercise exercise;
    private Workout workout;
    private WorkoutSet workoutSet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");

        exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setCategory("Chest");

        workout = new Workout();
        workout.setId(1L);
        workout.setUser(user);

        workoutSet = new WorkoutSet();
        workoutSet.setId(1L);
        workoutSet.setExercise(exercise);
        workoutSet.setWorkout(workout);
        workoutSet.setWeight(new BigDecimal("100.0"));
        workoutSet.setReps(8);
        workoutSet.setSetNumber(1);
    }

    // ── checkForPersonalRecord ────────────────────────────────────────────────

    @Test
    @DisplayName("First ever set for exercise creates a new PR")
    void checkForPersonalRecord_noPreviousPr_createsNewPr() {
        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.empty());

        boolean result = personalRecordService.checkForPersonalRecord(workoutSet, LocalDate.now());

        assertThat(result).isTrue();
        verify(personalRecordRepository, times(1)).save(any(PersonalRecord.class));
    }

    @Test
    @DisplayName("Heavier weight than current PR creates new PR")
    void checkForPersonalRecord_heavierWeight_createsNewPr() {
        PersonalRecord existingPr = buildExistingPr(new BigDecimal("80.0"), 8);

        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.of(existingPr));

        // workoutSet has 100kg which is heavier than existing 80kg
        boolean result = personalRecordService.checkForPersonalRecord(workoutSet, LocalDate.now());

        assertThat(result).isTrue();
        verify(personalRecordRepository, times(1)).save(any(PersonalRecord.class));
    }

    @Test
    @DisplayName("Equal weight to current PR does not create new PR")
    void checkForPersonalRecord_equalWeight_doesNotCreatePr() {
        PersonalRecord existingPr = buildExistingPr(new BigDecimal("100.0"), 8);

        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.of(existingPr));

        // workoutSet has 100kg which equals existing 100kg
        boolean result = personalRecordService.checkForPersonalRecord(workoutSet, LocalDate.now());

        assertThat(result).isFalse();
        verify(personalRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lighter weight than current PR does not create new PR")
    void checkForPersonalRecord_lighterWeight_doesNotCreatePr() {
        PersonalRecord existingPr = buildExistingPr(new BigDecimal("120.0"), 8);

        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.of(existingPr));

        // workoutSet has 100kg which is lighter than existing 120kg
        boolean result = personalRecordService.checkForPersonalRecord(workoutSet, LocalDate.now());

        assertThat(result).isFalse();
        verify(personalRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("New PR uses log date as achievedAt")
    void checkForPersonalRecord_newPr_usesLogDate() {
        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.empty());

        LocalDate logDate = LocalDate.of(2026, 1, 15);
        personalRecordService.checkForPersonalRecord(workoutSet, logDate);

        verify(personalRecordRepository).save(argThat(pr ->
                pr.getAchievedAt().equals(logDate.atStartOfDay())
        ));
    }

    @Test
    @DisplayName("New PR with null log date uses current time")
    void checkForPersonalRecord_nullLogDate_usesCurrentTime() {
        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.empty());

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        personalRecordService.checkForPersonalRecord(workoutSet, null);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        verify(personalRecordRepository).save(argThat(pr ->
                pr.getAchievedAt().isAfter(before) && pr.getAchievedAt().isBefore(after)
        ));
    }

    @Test
    @DisplayName("Existing PR is updated not duplicated")
    void checkForPersonalRecord_existingPr_updatesInPlace() {
        PersonalRecord existingPr = buildExistingPr(new BigDecimal("80.0"), 8);

        when(personalRecordRepository.findByUserIdAndExerciseId(1L, 1L))
                .thenReturn(Optional.of(existingPr));

        personalRecordService.checkForPersonalRecord(workoutSet, LocalDate.now());

        // should save the existing PR object, not create a new one
        verify(personalRecordRepository).save(existingPr);
        assertThat(existingPr.getWeight()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(existingPr.getReps()).isEqualTo(8);
    }

    // ── getPersonalRecordByUserId ─────────────────────────────────────────────

    @Test
    @DisplayName("Returns mapped DTOs ordered by achievedAt desc")
    void getPersonalRecordByUserId_returnsMappedDtos() {
        PersonalRecord pr1 = buildExistingPr(new BigDecimal("100.0"), 8);
        PersonalRecord pr2 = buildExistingPr(new BigDecimal("80.0"), 10);

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L))
                .thenReturn(List.of(pr1, pr2));

        List<PersonalRecordDto> result = personalRecordService.getPersonalRecordByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWeight()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(result.get(1).getWeight()).isEqualByComparingTo(new BigDecimal("80.0"));
    }

    @Test
    @DisplayName("Returns empty list when user has no PRs")
    void getPersonalRecordByUserId_noPrs_returnsEmptyList() {
        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L))
                .thenReturn(List.of());

        List<PersonalRecordDto> result = personalRecordService.getPersonalRecordByUserId(1L);

        assertThat(result).isEmpty();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private PersonalRecord buildExistingPr(BigDecimal weight, int reps) {
        PersonalRecord pr = PersonalRecord.builder()
                .user(user)
                .exercise(exercise)
                .workout(workout)
                .weight(weight)
                .reps(reps)
                .achievedAt(LocalDateTime.now().minusDays(7))
                .build();
        pr.setId(1L);
        return pr;
    }
}
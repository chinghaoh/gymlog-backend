package com.gymlog.dashboard;

import com.gymlog.record.PersonalRecord;
import com.gymlog.record.PersonalRecordRepository;
import com.gymlog.workoutlog.WorkoutLog;
import com.gymlog.workoutlog.WorkoutLogRepository;
import com.gymlog.user.User;
import com.gymlog.workout.Workout;
import com.gymlog.workout.SplitCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private WorkoutLogRepository workoutLogRepository;
    @Mock private PersonalRecordRepository personalRecordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Workout workout;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        workout = new Workout();
        workout.setId(1L);
        workout.setName("Push Day");
        workout.setSplitCategory(SplitCategory.PUSH);
        workout.setUser(user);
    }

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Returns correct total PRs count")
    void getSummary_returnsTotalPrs() {
        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L))
                .thenReturn(List.of(new PersonalRecord(), new PersonalRecord(), new PersonalRecord()));
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of());

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getTotalPrs()).isEqualTo(3);
    }

    @Test
    @DisplayName("Returns correct workouts this week count")
    void getSummary_returnsWorkoutsThisWeek() {
        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L))
                .thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any()))
                .thenReturn(List.of(buildLog(LocalDate.now()), buildLog(LocalDate.now().minusDays(1))));
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of());

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getWorkoutsThisWeek()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns zero summary when user has no data")
    void getSummary_noData_returnsZeros() {
        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L))
                .thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of());

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getTotalPrs()).isEqualTo(0);
        assertThat(result.getWorkoutsThisWeek()).isEqualTo(0);
        assertThat(result.getCurrentStreak()).isEqualTo(0);
    }

    // ── calculateStreak ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Streak is 0 when no logs exist")
    void calculateStreak_noLogs_returnsZero() {
        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(List.of());

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getCurrentStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("Streak is 1 when only today has a log")
    void calculateStreak_onlyToday_returnsOne() {
        List<WorkoutLog> logs = List.of(buildLog(LocalDate.now()));

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(logs);

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getCurrentStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("Streak is 3 when last 3 consecutive days have logs")
    void calculateStreak_threeDays_returnsThree() {
        List<WorkoutLog> logs = List.of(
                buildLog(LocalDate.now()),
                buildLog(LocalDate.now().minusDays(1)),
                buildLog(LocalDate.now().minusDays(2))
        );

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(logs);

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getCurrentStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("Streak breaks when there is a gap in days")
    void calculateStreak_gapInDays_streakBreaks() {
        List<WorkoutLog> logs = List.of(
                buildLog(LocalDate.now()),
                buildLog(LocalDate.now().minusDays(1)),
                // gap — day 3 missing
                buildLog(LocalDate.now().minusDays(3))
        );

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(logs);

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        // streak should be 2, not 3 — gap breaks the streak
        assertThat(result.getCurrentStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("Streak is 0 when last log was 2 days ago")
    void calculateStreak_lastLogTwoDaysAgo_returnsZero() {
        List<WorkoutLog> logs = List.of(
                buildLog(LocalDate.now().minusDays(2)),
                buildLog(LocalDate.now().minusDays(3))
        );

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(logs);

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        // today has no log so streak is 0
        assertThat(result.getCurrentStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple logs on same day count as one streak day")
    void calculateStreak_multipleLogsOnSameDay_countsAsOne() {
        List<WorkoutLog> logs = List.of(
                buildLog(LocalDate.now()),
                buildLog(LocalDate.now()),       // duplicate today
                buildLog(LocalDate.now().minusDays(1))
        );

        when(personalRecordRepository.findByUserIdOrderByAchievedAtDesc(1L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndDateBetweenOrderByDateDesc(eq(1L), any(), any())).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(logs);

        DashboardSummaryDto result = dashboardService.getSummary(1L);

        assertThat(result.getCurrentStreak()).isEqualTo(2);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private WorkoutLog buildLog(LocalDate date) {
        WorkoutLog log = WorkoutLog.builder()
                .user(user)
                .workout(workout)
                .date(date)
                .build();
        log.setId((long) (Math.random() * 1000));
        return log;
    }
}
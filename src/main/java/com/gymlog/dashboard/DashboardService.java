package com.gymlog.dashboard;

import com.gymlog.record.PersonalRecordRepository;
import com.gymlog.workout.WorkoutRepository;
import com.gymlog.workoutlog.WorkoutLog;
import com.gymlog.workoutlog.WorkoutLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkoutLogRepository workoutLogRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public DashboardSummaryDto getSummary(Long userId) {
        // Total PRs
        long totalPrs = personalRecordRepository
                .findByUserIdOrderByAchievedAtDesc(userId)
                .size();

        // Workouts this week
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        long workoutsThisWeek = workoutLogRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startOfWeek, endOfWeek)
                .size();

        List<WorkoutLog> logs = workoutLogRepository.findByUserIdOrderByDateDesc(userId);
        long currentStreak = calculateStreak(logs);

        return new DashboardSummaryDto(workoutsThisWeek, totalPrs, currentStreak);
    }

    private long calculateStreak(List<WorkoutLog> logs) {
        if (logs.isEmpty()) return 0;

        Set<LocalDate> loggedDates = logs.stream()
                .map(WorkoutLog::getDate)
                .collect(Collectors.toSet());

        long streak = 0;
        LocalDate date = LocalDate.now();

        while (loggedDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }

        return streak;
    }
}
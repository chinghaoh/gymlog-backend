package com.gymlog.dashboard;

import com.gymlog.record.PersonalRecordRepository;
import com.gymlog.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkoutRepository workoutRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public DashboardSummaryDto getSummary(Long userId) {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate today = LocalDate.now();

        long workoutsThisWeek = workoutRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startOfWeek, today)
                .size();

        long totalPrs = personalRecordRepository
                .findByUserIdOrderByAchievedAtDesc(userId)
                .size();

        long currentStreak = calculateStreak(userId, today);

        return new DashboardSummaryDto(workoutsThisWeek, totalPrs, currentStreak);
    }

    private long calculateStreak(Long userId, LocalDate today) {
        LocalDate date = today;
        long streak = 0;

        while (true) {
            LocalDate finalDate = date;
            boolean workedOut = !workoutRepository
                    .findByUserIdAndDateBetweenOrderByDateDesc(userId, finalDate, finalDate)
                    .isEmpty();
            if (!workedOut) break;
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}
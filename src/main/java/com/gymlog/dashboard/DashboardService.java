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
        long totalPrs = personalRecordRepository
                .findByUserIdOrderByAchievedAtDesc(userId)
                .size();

        return new DashboardSummaryDto(0L, totalPrs, 0L);
    }
}
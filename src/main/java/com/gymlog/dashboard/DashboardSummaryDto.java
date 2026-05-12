package com.gymlog.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryDto {
    private long workoutsThisWeek;
    private long totalPrs;
    private long currentStreak;
}
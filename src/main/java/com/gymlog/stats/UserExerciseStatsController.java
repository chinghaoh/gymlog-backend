package com.gymlog.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UserExerciseStatsController {

    private final UserExerciseStatsService statsService;

    @GetMapping
    public ResponseEntity<List<UserExerciseStatsDto>> getStatsByUser(
            @RequestParam Long userId) {
        return ResponseEntity.ok(statsService.getStatsByUserId(userId));
    }

    @GetMapping("/exercise")
    public ResponseEntity<UserExerciseStatsDto> getStatsByUserAndExercise(
            @RequestParam Long userId,
            @RequestParam Long exerciseId) {
        return ResponseEntity.ok(
                statsService.getStatsByUserAndExercise(userId, exerciseId));
    }
}
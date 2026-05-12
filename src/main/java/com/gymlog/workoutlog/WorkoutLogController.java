package com.gymlog.workoutlog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workoutlogs")
@RequiredArgsConstructor
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;

    @GetMapping
    public ResponseEntity<List<WorkoutLogDto>> getLogs(@RequestParam Long userId) {
        return ResponseEntity.ok(workoutLogService.getLogsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<WorkoutLogDto> createLog(
            @RequestParam Long userId,
            @RequestBody WorkoutLogDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutLogService.createLog(userId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        workoutLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
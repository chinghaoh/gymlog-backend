package com.gymlog.workout;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<List<WorkoutDto>> getWorkoutsByUser(
            @RequestParam Long userId) {
        return ResponseEntity.ok(workoutService.getWorkoutsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutDto> getWorkoutById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutById(id));
    }

    @GetMapping("/range")
    public ResponseEntity<List<WorkoutDto>> getWorkoutsByDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(
                workoutService.getWorkoutsByDateRange(userId, start, end));
    }

    @PostMapping
    public ResponseEntity<WorkoutDto> createWorkout(
            @RequestParam Long userId,
            @RequestBody WorkoutDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.createWorkout(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutDto> updateWorkout(
            @PathVariable Long id,
            @RequestBody WorkoutDto dto) {
        return ResponseEntity.ok(workoutService.updateWorkout(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }
}
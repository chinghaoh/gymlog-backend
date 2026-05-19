package com.gymlog.workout;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<List<WorkoutDto>> getWorkoutsByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(workoutService.getWorkoutsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutDto> getWorkoutById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutById(id));
    }

    @PostMapping
    public ResponseEntity<WorkoutDto> createWorkout(
            @RequestParam Long userId,
            @Valid @RequestBody WorkoutDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.createWorkout(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutDto> updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutDto dto) {
        return ResponseEntity.ok(workoutService.updateWorkout(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/range")
    public ResponseEntity<List<WorkoutDto>> getWorkoutsByRange(
            @RequestParam Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        return ResponseEntity.ok(workoutService.getWorkoutsByRange(userId, start, end));
    }
}
package com.gymlog.set;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sets")
@RequiredArgsConstructor
public class WorkoutSetController {

    private final WorkoutSetService workoutSetService;

    @GetMapping
    public ResponseEntity<List<WorkoutSetDto>> getSetsByWorkout(
            @RequestParam Long workoutId) {
        return ResponseEntity.ok(workoutSetService.getSetsByWorkoutId(workoutId));
    }

    @PostMapping
    public ResponseEntity<WorkoutSetDto> addSet(
            @RequestParam Long workoutId,
            @RequestParam Long exerciseId,
            @RequestBody WorkoutSetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutSetService.addSet(workoutId, exerciseId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSetDto> updateSet(
            @PathVariable Long id,
            @RequestBody WorkoutSetDto dto) {
        return ResponseEntity.ok(workoutSetService.updateSet(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSet(@PathVariable Long id) {
        workoutSetService.deleteSet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-exercise")
    public ResponseEntity<List<WorkoutSetDto>> getSetsByUserAndExercise(
            @RequestParam Long userId,
            @RequestParam Long exerciseId) {
        return ResponseEntity.ok(workoutSetService.getSetsByUserAndExercise(userId, exerciseId));
    }
}
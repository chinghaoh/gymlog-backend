package com.gymlog.exercise;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<List<ExerciseDto>> getAllExercises(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(
                exerciseService.getExercises(category, equipment, difficulty, name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDto> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }

    @PostMapping
    public ResponseEntity<ExerciseDto> createExercise(@RequestBody ExerciseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exerciseService.createExercise(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseDto> updateExercise(
            @PathVariable Long id,
            @RequestBody ExerciseDto dto) {
        return ResponseEntity.ok(exerciseService.updateExercise(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateExercise(@PathVariable Long id) {
        exerciseService.deactivateExercise(id);
        return ResponseEntity.noContent().build();
    }
}
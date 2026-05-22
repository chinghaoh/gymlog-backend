package com.gymlog.ai.creator;

import com.fasterxml.jackson.databind.JsonNode;
import com.gymlog.exercise.Exercise;
import com.gymlog.exercise.ExerciseRepository;
import com.gymlog.set.WorkoutSet;
import com.gymlog.set.WorkoutSetRepository;
import com.gymlog.user.User;
import com.gymlog.workout.SplitCategory;
import com.gymlog.workout.Workout;
import com.gymlog.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AiWorkoutCreator {

    private final WorkoutRepository workoutRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;

    public Long create(JsonNode plan, User user, SplitCategory split) {

        int totalSets = 0;
        for (JsonNode ex : plan.get("exercises")) {
            totalSets += ex.get("sets").asInt();
        }
        int estimatedDuration = totalSets * 3;

        Workout workout = Workout.builder()
                .user(user)
                .name(plan.get("workoutName").asText())
                .splitCategory(split)
                .aiGenerated(true)
                .notes(plan.get("reasoning").asText())
                .durationMinutes(estimatedDuration)
                .build();

        workout = workoutRepository.save(workout);

        for (JsonNode ex : plan.get("exercises")) {
            Exercise exercise = exerciseRepository.findById(ex.get("exerciseId").asLong())
                    .orElseThrow();

            int sets = ex.get("sets").asInt();
            int reps = ex.get("reps").asInt();
            double weight = ex.get("weight").asDouble();

            for (int i = 1; i <= sets; i++) {
                WorkoutSet set = WorkoutSet.builder()
                        .workout(workout)
                        .exercise(exercise)
                        .setNumber(i)
                        .reps(reps)
                        .weight(BigDecimal.valueOf(weight))
                        .build();
                workoutSetRepository.save(set);
            }
        }

        return workout.getId();
    }
}

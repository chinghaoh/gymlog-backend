package com.gymlog.ai.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.gymlog.common.AppException;
import com.gymlog.exercise.Exercise;
import com.gymlog.record.PersonalRecord;
import com.gymlog.workout.SplitCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AiWorkoutValidator {

    public void validate(JsonNode plan, SplitCategory split,
                         List<Exercise> exercises, List<PersonalRecord> prs) {

        // 1. Validate splitCategory matches what user requested
        String returnedSplit = plan.get("splitCategory").asText();
        if (!split.name().equals(returnedSplit)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI returned wrong split category. Please try again.");
        }

        // 2. Validate all exerciseIds exist in the list we sent
        Set<Long> validIds = exercises.stream()
                .map(Exercise::getId)
                .collect(Collectors.toSet());

        for (JsonNode ex : plan.get("exercises")) {
            Long exerciseId = ex.get("exerciseId").asLong();
            if (!validIds.contains(exerciseId)) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "AI referenced an invalid exercise. Please try again.");
            }
        }
    }
}
package com.gymlog.ai.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gymlog.common.AppException;
import com.gymlog.exercise.Exercise;
import com.gymlog.record.PersonalRecord;
import com.gymlog.workout.SplitCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AiWorkoutValidatorTest {

    private AiWorkoutValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        validator = new AiWorkoutValidator();
        objectMapper = new ObjectMapper();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ObjectNode buildPlan(String splitCategory, Long... exerciseIds) {
        ObjectNode plan = objectMapper.createObjectNode();
        plan.put("splitCategory", splitCategory);

        ArrayNode exercises = objectMapper.createArrayNode();
        for (Long id : exerciseIds) {
            ObjectNode ex = objectMapper.createObjectNode();
            ex.put("exerciseId", id);
            exercises.add(ex);
        }
        plan.set("exercises", exercises);
        return plan;
    }

    private Exercise buildExercise(Long id, String name) {
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName(name);
        return ex;
    }

    // ── Split category validation ─────────────────────────────────────────────

    @Test
    @DisplayName("Valid plan passes validation")
    void validate_validPlan_passes() {
        var plan = buildPlan("PUSH", 1L, 2L);
        var exercises = List.of(buildExercise(1L, "Bench Press"), buildExercise(2L, "Shoulder Press"));

        assertThatNoException().isThrownBy(() ->
                validator.validate(plan, SplitCategory.PUSH, exercises, List.of())
        );
    }

    @Test
    @DisplayName("Wrong split category throws AppException")
    void validate_wrongSplitCategory_throwsAppException() {
        var plan = buildPlan("PULL", 1L);
        var exercises = List.of(buildExercise(1L, "Bench Press"));

        assertThatThrownBy(() ->
                validator.validate(plan, SplitCategory.PUSH, exercises, List.of())
        )
                .isInstanceOf(AppException.class)
                .hasMessageContaining("wrong split category");
    }

    @Test
    @DisplayName("All split categories validate correctly")
    void validate_allSplitCategories_pass() {
        for (SplitCategory split : SplitCategory.values()) {
            var plan = buildPlan(split.name(), 1L);
            var exercises = List.of(buildExercise(1L, "Exercise"));

            assertThatNoException().isThrownBy(() ->
                    validator.validate(plan, split, exercises, List.of())
            );
        }
    }

    // ── Exercise ID validation ────────────────────────────────────────────────

    @Test
    @DisplayName("Invalid exercise ID throws AppException")
    void validate_invalidExerciseId_throwsAppException() {
        var plan = buildPlan("PUSH", 999L); // 999 not in valid exercises
        var exercises = List.of(buildExercise(1L, "Bench Press"));

        assertThatThrownBy(() ->
                validator.validate(plan, SplitCategory.PUSH, exercises, List.of())
        )
                .isInstanceOf(AppException.class)
                .hasMessageContaining("invalid exercise");
    }

    @Test
    @DisplayName("Mix of valid and invalid exercise IDs throws AppException")
    void validate_mixedExerciseIds_throwsAppException() {
        var plan = buildPlan("PUSH", 1L, 999L); // 999 is invalid
        var exercises = List.of(buildExercise(1L, "Bench Press"), buildExercise(2L, "Shoulder Press"));

        assertThatThrownBy(() ->
                validator.validate(plan, SplitCategory.PUSH, exercises, List.of())
        )
                .isInstanceOf(AppException.class)
                .hasMessageContaining("invalid exercise");
    }

    @Test
    @DisplayName("Multiple valid exercise IDs pass validation")
    void validate_multipleValidExerciseIds_passes() {
        var plan = buildPlan("LEGS", 1L, 2L, 3L);
        var exercises = List.of(
                buildExercise(1L, "Squat"),
                buildExercise(2L, "Leg Press"),
                buildExercise(3L, "Leg Curl")
        );

        assertThatNoException().isThrownBy(() ->
                validator.validate(plan, SplitCategory.LEGS, exercises, List.of())
        );
    }

    @Test
    @DisplayName("Empty exercise list in plan passes validation")
    void validate_emptyExercisesInPlan_passes() {
        var plan = buildPlan("PUSH"); // no exercises
        var exercises = List.of(buildExercise(1L, "Bench Press"));

        assertThatNoException().isThrownBy(() ->
                validator.validate(plan, SplitCategory.PUSH, exercises, List.of())
        );
    }

    @Test
    @DisplayName("Single exercise validates correctly")
    void validate_singleExercise_passes() {
        var plan = buildPlan("PULL", 5L);
        var exercises = List.of(buildExercise(5L, "Pull Up"));

        assertThatNoException().isThrownBy(() ->
                validator.validate(plan, SplitCategory.PULL, exercises, List.of())
        );
    }
}
package com.gymlog.ai.chat;

import com.gymlog.ai.builder.AiPromptBuilder;
import com.gymlog.ai.parser.AiResponseParser;
import com.gymlog.ai.creator.AiWorkoutCreator;
import com.gymlog.ai.validator.AiWorkoutValidator;
import com.gymlog.common.AppException;
import com.gymlog.exercise.Exercise;
import com.gymlog.exercise.ExerciseRepository;
import com.gymlog.record.PersonalRecord;
import com.gymlog.record.PersonalRecordRepository;
import com.gymlog.set.WorkoutSetRepository;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import com.gymlog.workout.SplitCategory;
import com.gymlog.workout.Workout;
import com.gymlog.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;



@Service
@RequiredArgsConstructor
public class AiChatService {

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final AiPromptBuilder promptBuilder;
    private final ChatClient chatClient;
    private final AiResponseParser responseParser;
    private final AiWorkoutValidator workoutValidator;
    private final AiWorkoutCreator workoutCreator;


    public AiChatResponse generateWorkout(AiChatRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "User not found"));

        SplitCategory split;

        try {
            split = SplitCategory.valueOf(request.splitCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Invalid split category: " + request.splitCategory());
        }

        List<Workout> recentSameSplit = workoutRepository
                .findTop3ByUserIdAndSplitCategoryOrderByCreatedAtDesc(request.userId(), split);

        List<String> categories = promptBuilder.getExerciseCategoriesForSplit(split);

        List<Exercise> exercises = exerciseRepository.findByCategoryInAndIsActiveTrue(categories);

        if (exercises.isEmpty()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No exercises found for this split. Please try again.");
        }

        List<PersonalRecord> prs = personalRecordRepository.findByUserIdOrderByAchievedAtDesc(request.userId());

        Set<Long> recentlyUsedIds = recentSameSplit.stream()
                .flatMap(w -> workoutSetRepository.findByWorkoutId(w.getId()).stream())
                .map(ws -> ws.getExercise().getId())
                .collect(Collectors.toSet());

        String prompt = promptBuilder.buildPrompt(user, split, exercises, prs, recentlyUsedIds);

        String rawResponse;
        try {
            rawResponse = chatClient.prompt()
                    .system(promptBuilder.buildSystemPrompt())
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI service unavailable. Please try again.");
        }

        JsonNode plan = responseParser.parse(rawResponse);

        if ("REJECTED".equals(plan.get("action").asText())) {
            return new AiChatResponse(
                    "REJECTED",
                    plan.get("message").asText(),
                    null,
                    null
            );
        }

        workoutValidator.validate(plan, split, exercises, prs);

        Long workoutId = workoutCreator.create(plan, user, split);

        return new AiChatResponse(
                "CREATE_WORKOUT",
                "Your " + plan.get("workoutName").asText() + " has been created!",
                plan.get("reasoning").asText(),
                workoutId
        );
    }

    public AiChatContextResponse getContext(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "User not found"));

        int totalWorkouts = workoutRepository.findByUserIdOrderByCreatedAtDesc(userId).size();

        return new AiChatContextResponse(
                totalWorkouts > 0,
                user.getFitnessLevel(),
                totalWorkouts
        );
    }
}

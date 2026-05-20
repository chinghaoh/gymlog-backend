package com.gymlog.ai.builder;

import com.gymlog.exercise.Exercise;
import com.gymlog.record.PersonalRecord;
import com.gymlog.user.User;
import com.gymlog.workout.SplitCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AiPromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are an experienced personal trainer with 10 years of experience.
                You think and act like a real trainer — you look at a client's history,
                personal records, and recent sessions to design smart progressive workouts.
                
                You can ONLY create workouts. If asked anything else respond with:
                {"action":"REJECTED","message":"I can only create workouts. Select a split to get started."}
                
                You MUST always respond in valid JSON only. No plain text. No markdown fences.
                """;
    }

    public String buildPrompt(User user, SplitCategory split,
                              List<Exercise> exercises,
                              List<PersonalRecord> prs,
                              Set<Long> recentlyUsedIds) {

        StringBuilder sb = new StringBuilder();

        sb.append("User: ").append(user.getName()).append("\n");
        sb.append("Fitness level: ").append(
                user.getFitnessLevel() != null ? user.getFitnessLevel() : "INTERMEDIATE"
        ).append("\n");
        sb.append("Requested split: ").append(split).append("\n\n");

        if (prs.isEmpty()) {
            sb.append("Personal records: none — use level-appropriate weights\n\n");
        } else {
            sb.append("Personal records:\n");
            prs.forEach(pr -> sb.append("- ")
                    .append(pr.getExercise().getName())
                    .append(" (id: ").append(pr.getExercise().getId()).append(")")
                    .append(": ").append(pr.getWeight()).append("kg x ")
                    .append(pr.getReps()).append(" reps")
                    .append(" (achieved: ").append(pr.getAchievedAt().toLocalDate()).append(")\n"));
            sb.append("\n");
        }

        if (!recentlyUsedIds.isEmpty()) {
            sb.append("Recently used exercises to avoid repeating:\n");
            exercises.stream()
                    .filter(e -> recentlyUsedIds.contains(e.getId()))
                    .forEach(e -> sb.append("- ").append(e.getName()).append("\n"));
            sb.append("\n");
        }

        sb.append("Available exercises for ").append(split).append(":\n");
        exercises.forEach(e -> sb.append("- id: ").append(e.getId())
                .append(", name: ").append(e.getName())
                .append(", category: ").append(e.getCategory()).append("\n"));
        sb.append("\n");

        sb.append("Rules:\n");
        sb.append("1. Only use exerciseIds from the list above\n");
        sb.append("2. Weight = 70-75% of PR weight for hypertrophy\n");
        sb.append("3. If PR is older than 3 months reduce suggested weight by 10%\n");
        sb.append("4. No PR for exercise = extrapolate from similar lifts or use level defaults\n");
        sb.append("5. Beginner defaults: compounds 20-40kg, dumbbells 8-12kg, isolation 5-10kg\n");
        sb.append("6. Intermediate defaults: compounds 60-80kg, dumbbells 15-25kg\n");
        sb.append("7. Advanced defaults: compounds 80kg+, dumbbells 25kg+\n");
        sb.append("8. Include 4-6 exercises, exactly 3 sets each, 8-12 reps\n");
        sb.append("9. Prefer exercises NOT in the recently used list\n");
        sb.append("10. Never exceed PR weight x 1.10\n\n");
        sb.append("IMPORTANT: Every exercise must have exactly 3 sets. No more, no less.\n\n");


        sb.append("Respond ONLY with this exact JSON structure:\n");
        sb.append("{\n");
        sb.append("  \"action\": \"CREATE_WORKOUT\",\n");
        sb.append("  \"workoutName\": \"...\",\n");
        sb.append("  \"splitCategory\": \"").append(split.name()).append("\",\n");
        sb.append("  \"reasoning\": \"...\",\n");
        sb.append("  \"exercises\": [\n");
        sb.append("    {\"exerciseId\": 0, \"exerciseName\": \"...\", \"sets\": 3, \"reps\": 8, \"weight\": 0.0}\n");
        sb.append("    {\"exerciseId\": 0, \"exerciseName\": \"...\", \"sets\": 3, \"reps\": 8, \"weight\": 0.0}\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    public List<String> getExerciseCategoriesForSplit(SplitCategory split) {
        return switch (split) {
            case PUSH -> List.of("Chest", "Shoulders", "Upper Arms");
            case PULL -> List.of("Back", "Upper Arms", "Lower Arms");
            case LEGS -> List.of("Upper Legs", "Lower Legs");
            case UPPER_BODY -> List.of("Chest", "Back", "Shoulders", "Upper Arms");
            case FULL_BODY -> List.of("Chest", "Back", "Shoulders", "Upper Legs", "Lower Legs");
            case CARDIO -> List.of("Cardio");
            case OTHER -> List.of("Waist", "Neck");
        };
    }
}
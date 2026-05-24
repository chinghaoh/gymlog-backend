package com.gymlog.exercise;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ExerciseSeeder {

    private final ExerciseRepository exerciseRepository;

    @Value("${workoutx.api.key}")
    private String apiKey;

    private static final List<String> BODY_PARTS = List.of(
            "Chest", "Back", "Shoulders", "Upper Arms",
            "Lower Arms", "Upper Legs", "Lower Legs",
            "Waist", "Cardio", "Neck"
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WorkoutXResponse {
        @JsonProperty("total") Integer total;
        @JsonProperty("count") Integer count;
        @JsonProperty("data") List<WorkoutXExercise> data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WorkoutXExercise {
        @JsonProperty("id") String id;
        @JsonProperty("name") String name;
        @JsonProperty("bodyPart") String bodyPart;
        @JsonProperty("equipment") String equipment;
        @JsonProperty("target") String target;
        @JsonProperty("secondaryMuscles") List<String> secondaryMuscles;
        @JsonProperty("gifUrl") String gifUrl;
        @JsonProperty("difficulty") String difficulty;
        @JsonProperty("category") String category;
        @JsonProperty("description") String description;
        @JsonProperty("instructions") List<String> instructions;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedExercises() throws Exception {
        if (exerciseRepository.existsByIsSeededTrue()) {
            return;
        }
        runSeed();
    }

    public void runSeed() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        for (String bodyPart : BODY_PARTS) {
            for (int offset = 0; offset <= 10; offset += 10) {
                String url = "https://api.workoutxapp.com/v1/exercises/bodyPart/"
                        + bodyPart.replace(" ", "%20")
                        + "?limit=10&offset=" + offset;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("X-WorkoutX-Key", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                WorkoutXResponse wrapper = mapper.readValue(
                        response.body(), WorkoutXResponse.class);

                if (wrapper.data == null || wrapper.data.isEmpty()) break;

                for (WorkoutXExercise ex : wrapper.data) {
                    if (!exerciseRepository.existsByNameIgnoreCase(ex.name)) {
                        exerciseRepository.save(mapToEntity(ex));
                    } else {
                        exerciseRepository.findByNameIgnoreCase(ex.name).ifPresent(existing -> {
                            existing.setInstructions(ex.instructions != null
                                    ? String.join("\n", ex.instructions)
                                    : null);
                            exerciseRepository.save(existing);
                        });
                    }
                }

                Thread.sleep(300);
            }
        }
    }

    public Long countExercises() {
        return exerciseRepository.count();
    }

    private Exercise mapToEntity(WorkoutXExercise ex) {
        return Exercise.builder()
                .name(ex.name)
                .category(ex.bodyPart)
                .equipment(ex.equipment)
                .targetMuscle(ex.target)
                .secondaryMuscles(
                        ex.secondaryMuscles != null
                                ? String.join(", ", ex.secondaryMuscles)
                                : null)
                .gifUrl(ex.gifUrl)
                .description(ex.description)
                .difficulty(ex.difficulty != null
                        ? ex.difficulty.toUpperCase()
                        : null)
                .exerciseType(ex.category)
                .instructions(ex.instructions != null
                        ? String.join("\n", ex.instructions)
                        : null)
                .isSeeded(true)
                .isActive(true)
                .aiCreated(false)
                .build();
    }
}
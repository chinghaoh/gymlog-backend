package com.gymlog.admin;

import com.gymlog.exercise.ExerciseSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExerciseSeeder exerciseSeeder;
    private final ChatClient chatClient;


    @PostMapping("/exercises/reseed")
    public ResponseEntity<String> reseedExercises() throws Exception {
        exerciseSeeder.runSeed();
        return ResponseEntity.ok("Exercise reseeding complete");
    }

    @GetMapping("/ai/test")
    public ResponseEntity<String> testAi() {
        String response = chatClient.prompt()
                .user("Say hello and confirm you are working. Keep it under 20 words.")
                .call()
                .content();
        return ResponseEntity.ok(response);
    }
}
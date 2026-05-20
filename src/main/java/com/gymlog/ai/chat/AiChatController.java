package com.gymlog.ai.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatService.generateWorkout(request));
    }

    @GetMapping("/chat/context")
    public ResponseEntity<AiChatContextResponse> getContext(@RequestParam Long userId) {
        return ResponseEntity.ok(aiChatService.getContext(userId));
    }
}
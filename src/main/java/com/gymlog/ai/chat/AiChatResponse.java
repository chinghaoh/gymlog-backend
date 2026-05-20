package com.gymlog.ai.chat;

public record AiChatResponse(
        String action,
        String message,
        String reasoning,
        Long workoutId
) {}
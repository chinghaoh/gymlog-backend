package com.gymlog.ai.chat;

public record AiChatContextResponse(
        boolean hasHistory,
        String fitnessLevel,
        int totalWorkouts
) {}

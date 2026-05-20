ALTER TABLE ai_coach_responses
DROP CONSTRAINT ai_response_type_check;

ALTER TABLE ai_coach_responses
    ADD CONSTRAINT ai_response_type_check
        CHECK (type IN ('WORKOUT_ANALYSIS', 'WEEKLY_PLAN', 'PR_TIP', 'CHAT'));
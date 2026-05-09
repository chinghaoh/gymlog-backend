CREATE TABLE ai_coach_responses
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    workout_id BIGINT      REFERENCES workouts (id) ON DELETE SET NULL,
    type       VARCHAR(50) NOT NULL
        CONSTRAINT ai_response_type_check
            CHECK (type IN ('WORKOUT_ANALYSIS', 'WEEKLY_PLAN', 'PR_TIP')),
    prompt     TEXT        NOT NULL,
    response   TEXT        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE ai_training_plans
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    week_starting DATE        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CONSTRAINT ai_plan_status_check
            CHECK (status IN ('PENDING', 'ACCEPTED', 'DISMISSED')),
    general_notes TEXT,
    generated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE ai_plan_days
(
    id        BIGSERIAL PRIMARY KEY,
    plan_id   BIGINT       NOT NULL REFERENCES ai_training_plans (id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL
        CONSTRAINT ai_plan_day_check
            CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    focus     VARCHAR(100),
    notes     TEXT
);

CREATE TABLE ai_plan_exercises
(
    id            BIGSERIAL PRIMARY KEY,
    day_id        BIGINT        NOT NULL REFERENCES ai_plan_days (id) ON DELETE CASCADE,
    exercise_id   BIGINT        NOT NULL REFERENCES exercises (id),
    sets          INT           NOT NULL,
    target_reps   INT           NOT NULL,
    target_weight DECIMAL(6,2),
    notes         TEXT
);

CREATE INDEX idx_ai_coach_responses_user_id ON ai_coach_responses (user_id);
CREATE INDEX idx_ai_training_plans_user_id ON ai_training_plans (user_id);
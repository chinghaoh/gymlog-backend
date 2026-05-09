CREATE TABLE user_exercise_stats
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exercise_id       BIGINT        NOT NULL REFERENCES exercises (id),
    total_sets        INT           NOT NULL DEFAULT 0,
    total_reps        INT           NOT NULL DEFAULT 0,
    total_volume      DECIMAL(10,2) NOT NULL DEFAULT 0,
    max_weight        DECIMAL(6,2)  NOT NULL DEFAULT 0,
    avg_reps_last5    DECIMAL(5,2),
    last_performed_at TIMESTAMP,
    CONSTRAINT uq_user_exercise_stats UNIQUE (user_id, exercise_id)
);

CREATE INDEX idx_user_exercise_stats_user_id ON user_exercise_stats (user_id);
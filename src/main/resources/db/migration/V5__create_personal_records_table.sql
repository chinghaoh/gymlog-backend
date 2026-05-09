CREATE TABLE personal_records
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exercise_id BIGINT        NOT NULL REFERENCES exercises (id),
    workout_id  BIGINT        NOT NULL REFERENCES workouts (id),
    weight      DECIMAL(6, 2) NOT NULL,
    reps        INT           NOT NULL,
    achieved_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_personal_records UNIQUE (user_id, exercise_id)
);

CREATE INDEX idx_personal_records_user_id ON personal_records (user_id);
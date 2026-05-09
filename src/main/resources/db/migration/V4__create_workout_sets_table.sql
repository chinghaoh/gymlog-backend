CREATE TABLE workout_sets
(
    id          BIGSERIAL PRIMARY KEY,
    workout_id  BIGINT         NOT NULL REFERENCES workouts (id) ON DELETE CASCADE,
    exercise_id BIGINT         NOT NULL REFERENCES exercises (id),
    set_number  INT            NOT NULL,
    reps        INT            NOT NULL,
    weight      DECIMAL(6, 2)  NOT NULL DEFAULT 0,
    notes       TEXT
);

CREATE INDEX idx_workout_sets_workout_id ON workout_sets (workout_id);
CREATE INDEX idx_workout_sets_exercise_id ON workout_sets (exercise_id);
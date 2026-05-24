CREATE TABLE workout_log_sets (
                                  id              BIGSERIAL PRIMARY KEY,
                                  workout_log_id  BIGINT NOT NULL REFERENCES workout_logs (id) ON DELETE CASCADE,
                                  exercise_id     BIGINT NOT NULL REFERENCES exercises (id),
                                  set_number      INTEGER NOT NULL,
                                  reps            INTEGER NOT NULL,
                                  weight          DECIMAL(6,2) NOT NULL,
                                  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workout_log_sets_log_id ON workout_log_sets (workout_log_id);
CREATE INDEX idx_workout_log_sets_exercise_id ON workout_log_sets (exercise_id);
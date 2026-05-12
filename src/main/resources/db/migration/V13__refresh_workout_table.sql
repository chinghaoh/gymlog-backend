-- Truncate test data
TRUNCATE personal_records, user_exercise_stats, workout_sets, workout_logs CASCADE;

-- Drop old tables
DROP TABLE IF EXISTS workout_templates CASCADE;

-- Rename workout_logs back to workouts
ALTER TABLE workout_logs RENAME TO workouts;

-- Remove template_id, date, energy_level columns from workouts
ALTER TABLE workouts DROP COLUMN IF EXISTS template_id;
ALTER TABLE workouts DROP COLUMN IF EXISTS date;
ALTER TABLE workouts DROP COLUMN IF EXISTS energy_level;

-- Fix workout_sets to reference workouts
ALTER TABLE workout_sets RENAME COLUMN workout_log_id TO workout_id;
ALTER TABLE workout_sets DROP CONSTRAINT IF EXISTS workout_sets_workout_log_id_fkey;
ALTER TABLE workout_sets ADD CONSTRAINT workout_sets_workout_id_fkey
    FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE;

-- Create workout_logs table (clean)
CREATE TABLE workout_logs (
                              id           BIGSERIAL PRIMARY KEY,
                              user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                              workout_id   BIGINT NOT NULL REFERENCES workouts (id) ON DELETE CASCADE,
                              date         DATE   NOT NULL,
                              energy_level INT    CONSTRAINT workout_logs_energy_check CHECK (energy_level BETWEEN 1 AND 10),
                              notes        TEXT,
                              created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workout_logs_user_id ON workout_logs (user_id);
CREATE INDEX idx_workout_logs_workout_id ON workout_logs (workout_id);
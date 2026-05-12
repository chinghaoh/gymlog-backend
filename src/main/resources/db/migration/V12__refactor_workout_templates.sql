-- Step 1: Create workout_templates table
CREATE TABLE workout_templates (
                                   id               BIGSERIAL PRIMARY KEY,
                                   user_id          BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                   name             VARCHAR(150) NOT NULL,
                                   split_category   VARCHAR(50)  NOT NULL
                                       CONSTRAINT templates_split_check
                                           CHECK (split_category IN ('PUSH', 'PULL', 'LEGS', 'UPPER_BODY', 'FULL_BODY', 'CARDIO', 'OTHER')),
                                   duration_minutes INT,
                                   notes            TEXT,
                                   created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workout_templates_user_id ON workout_templates (user_id);

-- Step 2: Migrate existing workouts into templates
INSERT INTO workout_templates (user_id, name, split_category, duration_minutes, notes, created_at)
SELECT user_id, name, split_category, duration_minutes, notes, created_at
FROM workouts;

-- Step 3: Add template_id to workouts (now workout_logs)
ALTER TABLE workouts ADD COLUMN template_id BIGINT REFERENCES workout_templates (id) ON DELETE SET NULL;

-- Step 4: Link existing workouts to their new templates
UPDATE workouts w
SET template_id = t.id
    FROM workout_templates t
WHERE t.user_id = w.user_id
  AND t.name = w.name
  AND t.split_category = w.split_category;

-- Step 5: Rename workouts to workout_logs
ALTER TABLE workouts RENAME TO workout_logs;

-- Step 6: Update foreign keys on related tables
ALTER TABLE workout_sets RENAME COLUMN workout_id TO workout_log_id;
ALTER TABLE workout_sets DROP CONSTRAINT workout_sets_workout_id_fkey;
ALTER TABLE workout_sets ADD CONSTRAINT workout_sets_workout_log_id_fkey
    FOREIGN KEY (workout_log_id) REFERENCES workout_logs (id) ON DELETE CASCADE;

ALTER TABLE personal_records RENAME COLUMN workout_id TO workout_log_id;
ALTER TABLE personal_records DROP CONSTRAINT personal_records_workout_id_fkey;
ALTER TABLE personal_records ADD CONSTRAINT personal_records_workout_log_id_fkey
    FOREIGN KEY (workout_log_id) REFERENCES workout_logs (id) ON DELETE CASCADE;
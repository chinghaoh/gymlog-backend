ALTER TABLE personal_records
DROP CONSTRAINT personal_records_workout_id_fkey;

ALTER TABLE personal_records
ADD CONSTRAINT personal_records_workout_id_fkey
FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE;
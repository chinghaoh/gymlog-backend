CREATE TABLE exercises
(
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(250) NOT NULL,
    category      VARCHAR(150) NOT NULL,
    equipment     VARCHAR(150),
    difficulty    VARCHAR(50)  CONSTRAINT difficulty_check CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
    exercise_type VARCHAR(100),
    description   TEXT,
    is_seeded     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_exercises_category ON exercises (category);
CREATE INDEX idx_exercises_name ON exercises (name);
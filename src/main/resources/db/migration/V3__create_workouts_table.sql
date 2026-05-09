CREATE TABLE workouts
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name             VARCHAR(150)  NOT NULL,
    split_category   VARCHAR(50)   NOT NULL
        CONSTRAINT workouts_split_check
            CHECK (split_category IN ('PUSH', 'PULL', 'LEGS', 'UPPER_BODY', 'FULL_BODY', 'CARDIO', 'OTHER')),
    date             DATE          NOT NULL,
    duration_minutes INT,
    energy_level     INT
        CONSTRAINT workouts_energy_check CHECK (energy_level BETWEEN 1 AND 10),
    notes            TEXT,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workouts_user_id ON workouts (user_id);
CREATE INDEX idx_workouts_user_date ON workouts (user_id, date);
ALTER TABLE users ADD COLUMN fitness_level VARCHAR(20)
    CONSTRAINT users_fitness_level_check
        CHECK (fitness_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'));
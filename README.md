# GymLog Backend

A Spring Boot REST API for the GymLog fitness tracking application. Handles authentication, workout/exercise management, personal records, AI-generated workouts, and email notifications.

## Tech Stack

- **Java 17** / **Spring Boot 4**
- **Spring Security** — JWT-based auth via HttpOnly cookies
- **Spring Data JPA** + **PostgreSQL** — persistence
- **Flyway** — database migrations
- **Spring AI (Anthropic)** — AI workout generation using Claude Haiku
- **Redis** — caching and rate limiting (Bucket4j + Redisson)
- **AWS S3** — asset storage
- **Spring Mail** — email verification and password reset
- **SpringDoc OpenAPI** — Swagger UI at `/swagger-ui.html`
- **Lombok** — boilerplate reduction

## Features

- **Auth** — register, login, logout, JWT cookie session, email verification, forgot/reset password, demo account
- **Workouts** — CRUD for workout templates and workout logs (split categories: PUSH, PULL, LEGS, UPPER_BODY, FULL_BODY, CARDIO, OTHER)
- **Exercises** — exercise library integrated via the WorkoutX API
- **Personal Records** — tracked per user per exercise
- **AI Coach** — generates personalised workouts based on split type, recent PRs, and training history; validates AI output before persisting
- **Actuator** — health, info, and metrics endpoints

## Project Structure

```
src/main/java/com/gymlog/
├── auth/          # AuthController, JwtService, JwtAuthFilter, SecurityConfig
├── user/          # UserController, UserService, UserDto
├── workout/       # WorkoutController, WorkoutService, SplitCategory
├── log/           # WorkoutLogController, WorkoutLogService
├── exercise/      # ExerciseController, ExerciseRepository
├── set/           # WorkoutSet, WorkoutSetRepository
├── record/        # PersonalRecord, PersonalRecordService
├── ai/
│   ├── AiController
│   ├── builder/   # AiPromptBuilder — builds Claude prompts
│   ├── creator/   # AiWorkoutCreator — persists AI plan to DB
│   └── validator/ # AiWorkoutValidator — validates AI response
└── common/        # AppException, global error handler
```

## Database Migrations (Flyway)

Migrations live in `src/main/resources/db/migration/`. Key tables:

| Table | Description |
|---|---|
| `users` | Registered users |
| `workouts` | Workout templates (name, split, duration) |
| `workout_logs` | Logged instances of a workout (date, energy level) |
| `workout_sets` | Individual sets per workout log |
| `exercises` | Exercise catalogue |
| `personal_records` | Best lifts per user per exercise |
| `ai_coach_responses` | Stored AI responses (analysis, plans, tips) |
| `ai_training_plans` | Weekly AI plans with day/exercise breakdown |

## Getting Started

### Prerequisites

- Java 17
- PostgreSQL
- Redis

### Environment Variables

Set the following (e.g. in `application-local.yml` or as OS env vars):

```env
DB_URL=jdbc:postgresql://localhost:5432/gymlog
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

JWT_SECRET=your_jwt_secret

ANTHROPIC_API_KEY=your_anthropic_key

MAIL_USERNAME=your_gmail_address
MAIL_PASSWORD=your_gmail_app_password

REDIS_HOST=localhost
REDIS_PORT=6379

WORKOUTX_API_KEY=your_workoutx_key

AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret

FRONTEND_URL=http://localhost:5173
CORS_ORIGINS=http://localhost:5173
APP_COOKIE_SECURE=false
CLOUDFRONT_URL=https://your-cloudfront-domain.cloudfront.net
```

### Run Locally

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`.
Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Build JAR

```bash
./mvnw clean package -DskipTests
```

## API Endpoints

### Auth — `/api/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Register new user |
| POST | `/login` | Login, sets JWT cookie |
| GET | `/me` | Get current authenticated user |
| POST | `/logout` | Clear JWT cookie |
| POST | `/demo` | Create and login as a demo user |
| GET | `/verify` | Verify email with token |
| POST | `/forgot-password` | Send password reset email |
| POST | `/reset-password` | Reset password with token |

All protected endpoints authenticate via the `jwt` HttpOnly cookie.

## Deployment

The backend is deployed to an **AWS EC2** instance via GitHub Actions on every push to `main`.

Required GitHub secrets:

| Secret | Description |
|---|---|
| `EC2_HOST` | EC2 public hostname |
| `EC2_USERNAME` | SSH username (e.g. `ec2-user`) |
| `EC2_SSH_KEY` | Private SSH key |

The workflow: sets up Java 17 (Corretto) → builds the JAR → uploads it to EC2 via SCP → restarts the Spring Boot process via SSH → health-checks `/actuator/health`.

Environment variables on EC2 are loaded from `~/.env` via `source ~/.env` before starting the app.

## Configuration Notes

- **JWT** expiration is 24 hours (`86400000` ms)
- **Redis cache** TTL is 24 hours (`86400000` ms)
- **AI model** is `claude-haiku-4-5-20251001` with max 1500 tokens
- **Flyway** validates schema on startup (`ddl-auto: validate`) — never auto-creates tables
- SMTP is Gmail with STARTTLS on port 587
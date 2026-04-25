# Repository Guidelines

## Project Structure & Module Organization
SenseCore is a multi-module monorepo with a Java backend and Angular frontend.
- `services/`: Maven modules (`contracts`, `persistence`, `ingestion-service`, `processor-service`, `query-service`). Java sources live in `src/main/java`, tests in `src/test/java`.
- `frontend/sensecore-web/`: Angular app (`src/app/features`, `src/app/api`, `src/app/shared`).
- `infra/`: local stack orchestration (`docker-compose.yaml`, `nginx/`, `rabbitmq/definitions.json`).
- `docs/diagrams/`: architecture and frontend design docs.

## Build, Test, and Development Commands
Backend (run from repo root):
- `mvn clean verify`: build all backend modules and run tests.
- `mvn -pl services/query-service -am spring-boot:run`: run one service with required modules (`ingestion-service` / `processor-service` work the same way).

Frontend (run from `frontend/sensecore-web`):
- `npm ci`: install locked dependencies.
- `npm start`: run Angular dev server on `http://localhost:4200`.
- `npm run build`: production build.
- `npm test`: unit tests (Vitest via Angular CLI).

Infra:
- `docker compose -f infra/docker-compose.yaml up -d`: start Postgres, RabbitMQ, services, and Nginx locally.

## Coding Style & Naming Conventions
- Java: 4-space indentation, package root `pl.pawel.sensecore`, class names in `PascalCase`, methods/fields in `camelCase`.
- TypeScript/Angular: 2-space indentation, file pattern `*.page.ts|html|scss`, classes in `PascalCase`, members in `camelCase`.
- Keep API and DTO names explicit (`TelemetryReadingDto`, `QueryApiService`).
- Frontend formatting follows Prettier settings in `frontend/sensecore-web/package.json` (`printWidth: 100`, single quotes).
- For frontend UI work, follow the styling rules and visual conventions defined in `DESIGN.md`.

## Testing Guidelines
- Backend uses JUnit 5 with Spring Boot Test; integration tests use Testcontainers (PostgreSQL, RabbitMQ).
- Name tests `*Test` and use descriptive method names with underscores (for example, `devices_returns_sorted_by_device_id`).
- Run backend tests with `mvn test` (module or root), frontend tests with `npm test`.

## Commit & Pull Request Guidelines
- Keep commit subjects short, sentence case, and action-oriented (examples in history: `Add ...`, `Update ...`, `Refactor ...`).
- Optionally include issue references (for example, `#4 ...`) when applicable.
- PRs should include: purpose, affected modules (`services/...`, `frontend/...`, `infra/...`), test evidence (command + result), and screenshots/GIFs for UI changes.

## Security & Configuration Tips
- Do not commit secrets or private certificates; keep local certs under `infra/nginx/certs` out of version control.
- Prefer environment variables in `docker-compose.yaml` / `application*.yaml` over hardcoded credentials.

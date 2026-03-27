# SenseCore
SenseCore is a modular IoT platform that enables secure device-to-cloud telemetry ingestion and real-time data visualization, designed to scale from a single sensor to a distributed multi-sensor environment.

## Local start + smoke test
From repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-start-smoke.ps1
```

What it does:
- starts infra and backend services from `infra/docker-compose.yaml`
- starts frontend dev server on `http://localhost:4200` (if not already running)
- runs smoke checks for:
  - `http://localhost:8080/health`
  - `http://localhost:8080/api/devices`
  - `http://localhost:8080/api/telemetry/latest`
  - `http://localhost:8080/api/telemetry/history`

Backend-only mode:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-start-smoke.ps1 -NoFrontend
```

Stop backend:
- `docker-compose -f infra/docker-compose.yaml down`

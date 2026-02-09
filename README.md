# SMsenderAPI

This repository contains a Spring Boot API and a React web UI for configuring the EV12 remote patient monitoring SOS button. The UI collects configuration values and the backend sends the corresponding SMS command sequence through Vonage (Nexmo).

## Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Environment variables for Vonage (see `backend/.env.example`):

- `VONAGE_API_KEY`
- `VONAGE_API_SECRET`
- `VONAGE_FROM_NUMBER`

Set `SMS_DRY_RUN=true` to log messages instead of sending real SMS (default is `false`). If you have not configured Vonage credentials yet, enable dry-run to prevent API errors.

The API endpoint is `POST /api/send-config`.

## Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080`. You can set `VITE_API_BASE` in `frontend/.env.example` when deploying the UI separately.

# SMsenderAPI

This repository contains a Spring Boot API and a React web UI for configuring the EV12 remote patient monitoring SOS button. The UI collects configuration values and the backend sends the corresponding SMS command sequence through Twilio.

## Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Environment variables for Twilio (see `backend/.env.example`):

- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_FROM_NUMBER`

Set `SMS_DRY_RUN=true` (default) to log messages instead of sending real SMS.

The API endpoint is `POST /api/send-config`.

## Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080`. You can set `VITE_API_BASE` in `frontend/.env.example` when deploying the UI separately.

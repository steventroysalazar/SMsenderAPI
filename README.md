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

Configuration commands are combined into a single SMS payload separated by semicolons. If the payload exceeds 150 characters, it is split into two messages (first 150 chars, then the remainder).

## Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080`. You can set `VITE_API_BASE` in `frontend/.env.example` when deploying the UI separately.

## Testing with Postman

1. Start the backend (`mvn spring-boot:run`).
2. Create a new request in Postman:
   - Method: `POST`
   - URL: `http://localhost:8080/api/send-config`
   - Headers: `Content-Type: application/json`
3. Use a JSON body like:

```json
{
  "deviceNumber": "+15551234567",
  "contactNumber": "+15550000001",
  "smsPassword": "123456",
  "requestLocation": true,
  "wifiEnabled": true,
  "micVolume": 10,
  "speakerVolume": 90,
  "prefixEnabled": true,
  "prefixName": "Emma",
  "checkBattery": true,
  "fallDownEnabled": true,
  "fallDownSensitivity": 5,
  "fallDownCall": true,
  "noMotionEnabled": true,
  "noMotionTime": "80M",
  "noMotionCall": true,
  "apnEnabled": true,
  "apn": "internet",
  "serverEnabled": true,
  "serverHost": "www.smart-locator.com",
  "serverPort": 6060,
  "gprsEnabled": true,
  "workingMode": "mode2",
  "workingModeInterval": "03M",
  "workingModeNoMotionInterval": "01H",
  "continuousLocateInterval": "10s",
  "continuousLocateDuration": "600s",
  "checkStatus": true
}
```

If you do not have Vonage credentials yet, set `SMS_DRY_RUN=true` so the request succeeds without sending real SMS.

## Receiving device replies

Configure Vonage inbound SMS webhook to:

```
POST http://<your-server>/api/inbound-sms
```

The frontend can poll `GET /api/inbound-messages` to display replies.

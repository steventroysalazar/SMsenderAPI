# SMsenderAPI

This repository contains a Spring Boot API and a React web UI for configuring the EV12 remote patient monitoring SOS button. The UI collects configuration values and the backend sends the corresponding SMS command sequence through a local SMS gateway service.

## Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Environment variables for the local gateway (see `backend/.env.example`):

- `LOCAL_SMS_SEND_URL`
- `LOCAL_SMS_AUTHORIZATION` (optional, raw value for `Authorization` header)
- `LOCAL_SMS_RECEIVE_URL` (optional, for polling inbound device replies)

Set `SMS_DRY_RUN=true` to log messages instead of sending real SMS (default is `false`). If your local gateway is not yet running, enable dry-run to validate command payloads first.

The API endpoint is `POST /api/send-config`.

Configuration commands are combined into a single SMS payload separated by semicolons. If the payload exceeds 150 characters, it is split into two messages (first 150 chars, then the remainder).

### Local gateway troubleshooting

If you get `502` from `/api/send-config`, your local SMS gateway endpoint is unreachable or returned a non-2xx response.

Try these in order:

1. Set `SMS_DRY_RUN=true` first to verify command construction works without gateway calls.
2. Confirm `LOCAL_SMS_SEND_URL` points to your local gateway send endpoint.
3. Verify your local gateway expects JSON body in this shape: `{"to":"...","message":"..."}`.
4. If your gateway requires auth, set `LOCAL_SMS_AUTHORIZATION` to the exact header value (for example your API key UUID).


### Incremental polling flow (device replies)

After pressing **Send configuration SMS**, the web app starts polling `GET /api/inbound-messages` every ~3 seconds using:

- `since=<unix_timestamp_from_send_click>`
- `limit=100`
- `phone=<device_number>`

Backend behavior:

- If `LOCAL_SMS_RECEIVE_URL` is set, backend fetches from your local gateway receive endpoint and filters by `since` + `phone`.
- If `LOCAL_SMS_RECEIVE_URL` is not set, backend falls back to messages stored via `POST /api/inbound-sms`.

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

If your local gateway is not ready yet, set `SMS_DRY_RUN=true` so the request succeeds without sending real SMS.

## Receiving device replies

Configure your SMS provider inbound webhook to:

```
POST http://<your-server>/api/inbound-sms
```

The frontend can poll `GET /api/inbound-messages` to display replies.

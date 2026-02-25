# HomeOps Grocery

A mobile-first grocery management system.

- **Backend**: FastAPI + PostgreSQL, running on a miniPC via Docker.
- **Android app**: Kotlin + Jetpack Compose, running on a physical phone over LAN.
- **Phone uploads photos** and consumes REST APIs.
- **Backend stores data** in Postgres and persists images on disk.

---

## Repository Layout

```
/
├── backend/          FastAPI application
│   ├── app/
│   │   ├── main.py
│   │   ├── models.py       SQLAlchemy models
│   │   ├── database.py
│   │   ├── schemas.py      Pydantic request/response models
│   │   ├── config.py
│   │   ├── routers/        One file per API group
│   │   └── services/       Business logic
│   ├── Dockerfile
│   ├── requirements.txt
│   └── .env.example
│
├── android/          Android Studio project (Kotlin + Compose)
│   └── app/src/main/java/com/homeops/grocery/
│       ├── config/AppConfig.kt   ← set your miniPC IP here
│       ├── network/              Retrofit client & API service
│       ├── ui/screens/           HomeScreen, CaptureScreen, …
│       └── viewmodel/
│
├── infra/
│   └── docker-compose.yml
│
├── docs/
│   └── api.md        Full API reference
│
└── README.md
```

---

## Running the Backend

### Prerequisites

- Docker and Docker Compose installed on the miniPC.
- Port **8000** open on the miniPC firewall (see Firewall section).

### Start

```bash
cd infra
docker compose up --build
```

The backend is now reachable at `http://<minipc-ip>:8000`.

The Postgres database is persisted in the `db_data` Docker named volume.
Uploaded images are persisted in the `image_data` Docker named volume (mapped to `/app/data` inside the backend container).

### Stop

```bash
docker compose down
```

To also delete all data volumes:

```bash
docker compose down -v
```

---

## Finding Your miniPC's IP Address

On the miniPC (Linux):

```bash
ip addr show | grep 'inet ' | grep -v 127.0.0.1
```

Look for an address on your LAN interface (e.g. `192.168.1.42`).

On macOS:

```bash
ipconfig getifaddr en0
```

---

## Testing from a Phone Browser

Once the backend is running, open this URL in a phone browser on the same WiFi network:

```
http://<minipc-ip>:8000/health
```

Expected response: `{"status":"ok"}`

You can also browse the interactive API docs at:

```
http://<minipc-ip>:8000/docs
```

---

## Setting the Android Base URL

Open `android/app/src/main/java/com/homeops/grocery/config/AppConfig.kt`:

```kotlin
object AppConfig {
    const val BASE_URL = "http://192.168.1.100:8000/"  // ← change this
}
```

Replace `192.168.1.100` with the actual IP address of your miniPC.
The trailing slash is **required**.

Then rebuild and install the app.

---

## Firewall – Opening Port 8000

### Linux (ufw)

```bash
sudo ufw allow 8000/tcp
sudo ufw reload
```

### Linux (firewalld)

```bash
sudo firewall-cmd --add-port=8000/tcp --permanent
sudo firewall-cmd --reload
```

### Windows (PowerShell, run as admin)

```powershell
New-NetFirewallRule -DisplayName "HomeOps Grocery API" -Direction Inbound -Protocol TCP -LocalPort 8000 -Action Allow
```

---

## Android Development

Open the `android/` folder in Android Studio (Electric Eel or newer).

1. Set `AppConfig.BASE_URL` to your miniPC's IP.
2. Enable developer mode and USB debugging on the phone.
3. Connect the phone, press **Run** in Android Studio.

The app uses cleartext HTTP for development convenience (`android:usesCleartextTraffic="true"` in the manifest). For production, serve the backend over HTTPS and remove this flag.

---

## API Reference

See [`docs/api.md`](docs/api.md) for the full endpoint reference.

---

## Architecture Notes

- **No Alembic**: Tables are auto-created at startup with `Base.metadata.create_all`. For schema migrations in production, add Alembic.
- **CORS**: All origins are allowed (`*`) for LAN convenience. Restrict in production.
- **Meal planner v1**: Deterministic round-robin of 7 template meals. The `meal_planner.py` service is structured so the `_plan_meals()` function can be replaced with an OpenAI API call without changing any other code.
- **Image storage**: Images are stored as flat files in the `image_data` Docker volume. The filename is a UUID hex string. No CDN or object store is used in v1.
- **Authentication**: None in v1. The backend is assumed to run on a trusted LAN. Add OAuth2 or API keys before exposing to the public internet.

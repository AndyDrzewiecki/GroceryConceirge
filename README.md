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

## Getting the Android APK

### Option A – Download from GitHub Actions (recommended)

Every push to any branch triggers the `Android Debug APK` workflow.

1. Go to the repository on GitHub.
2. Click the **Actions** tab.
3. Select the latest `Android Debug APK` run.
4. Scroll to the **Artifacts** section at the bottom of the run summary.
5. Download `homeops-grocery-debug-apk` (a ZIP containing `app-debug.apk`).

### Option B – Build locally

```bash
# Windows
cd android && .\gradlew.bat :app:assembleDebug

# Linux / macOS
cd android && ./gradlew :app:assembleDebug
```

Output: `android/app/build/outputs/apk/debug/app-debug.apk`

### Install on phone

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

---

## Setting the Backend URL at Runtime

The app ships with the emulator default (`http://10.0.2.2:8000/`).
**No personal IP is committed to source control.**

To point the app at your miniPC on a physical phone:

1. Open the app on the phone.
2. Tap the **gear icon** (⚙) in the top-right corner of the Home screen.
3. Enter your miniPC's LAN URL, e.g. `http://192.168.1.42:8000/`
4. Tap **Save**.
5. Return to Home and tap **Test Connection** – you should see `OK`.

The URL is stored in SharedPreferences and persists between app restarts.

---

## Quick End-to-End Test

```
a) Start backend:      cd infra && docker compose up --build
b) Phone on same Wi-Fi as miniPC
c) In app: Settings → set BASE_URL to http://<ZBOOK-IP>:8000/
d) Home → tap "Test Connection"  →  expects "OK – http://... → status: ok"
```

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

Open the `android/` folder in Android Studio (Hedgehog or newer).

1. Enable developer mode and USB debugging on the phone.
2. Connect the phone, press **Run** in Android Studio.
3. On first launch, tap the gear icon and enter your backend URL.

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

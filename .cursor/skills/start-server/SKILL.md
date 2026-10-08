---
name: start-server
description: Starts SpendSense local dev servers — Spring Boot backend and Vite frontend together. Restarts and rebuilds when both are already running. Use when the user says "start server", "run servers", "start the app", "run locally", "restart server", or wants both backend and frontend running for spendsense development.
---

# Start Server

Starts **both** SpendSense dev servers. Do not start only one unless the user asks for backend or frontend alone.

**Plan reference:** [.cursor/plans/expense_tracker_mvp_d9134402.plan.md](../../plans/expense_tracker_mvp_d9134402.plan.md) — Frontend Stack Highlights (CORS `http://localhost:5173`); Local dev URLs below.

## Local URLs

| Service | URL | Health / entry |
|---------|-----|----------------|
| Backend | `http://localhost:8080` | `GET /api/health` |
| Frontend | `http://localhost:5173` | App root → login or dashboard |

Frontend API client defaults to `http://localhost:8080` via `VITE_API_URL` (see `spendsense/frontend/src/lib/config.ts`).

## Workflow

### Step 1 — Detect running servers

Check the terminals folder **and** whether ports are in use:

| Service | Port | Typical process |
|---------|------|-----------------|
| Backend | 8080 | Java / `spring-boot:run` |
| Frontend | 5173 | Node / Vite (`npm run dev`) |

### Step 2 — Branch on state

**Both backend and frontend are running** → follow **Restart with build** below.

**One or neither running** → follow **Cold start** below.

---

### Restart with build (both already running)

1. **Stop backend** — free port 8080.
2. **Stop frontend** — free port 5173.
3. **Build backend** — compile latest Java sources.
4. **Build frontend** — TypeScript check + Vite production build.
5. **Start backend** in a background shell.
6. **Start frontend** in a background shell (`npm run dev`).
7. **Confirm** — poll `GET /api/health` until `200` or report errors.
8. **Tell the user** both URLs and that servers were restarted after a fresh build.

### Cold start (default)

1. If port 8080 is free, **start backend** in a background shell; otherwise skip backend start.
2. If port 5173 is free, **start frontend** in a background shell; otherwise skip frontend start.
3. **Confirm** backend health if backend was started.
4. **Tell the user** which URLs are up.

Run each server in its own background terminal (`block_until_ms: 0`).

---

## Commands

### Windows (default for this workspace)

**Stop servers (restart path only):**

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
```

**Build backend:**

```powershell
cd spendsense\backend
.\mvnw.cmd compile
```

**Build frontend:**

```powershell
cd spendsense\frontend
npm run build
```

**Start backend:**

```powershell
cd spendsense\backend
.\mvnw.cmd spring-boot:run
```

**Start frontend:**

```powershell
cd spendsense\frontend
npm run dev
```

### macOS / Linux

**Stop servers:**

```bash
lsof -ti:8080 | xargs -r kill -9
lsof -ti:5173 | xargs -r kill -9
```

**Build + start:**

```bash
cd spendsense/backend && ./mvnw compile && ./mvnw spring-boot:run
```

```bash
cd spendsense/frontend && npm run build && npm run dev
```

(Run backend and frontend in separate background shells.)

## Prerequisites

- Java 17+ for backend
- Node.js 20+ for frontend
- First-time frontend: run `npm install` in `spendsense/frontend` if `node_modules` is missing

## Verify backend is up

```powershell
curl http://localhost:8080/api/health
```

Expected: `{"status":"UP","app":"SpendSense"}` (or equivalent).

## Troubleshooting

| Issue | Action |
|-------|--------|
| Port still in use after stop | Wait 2–3s and retry stop; identify process with `netstat -ano \| findstr :8080` |
| Backend build fails | Fix compile errors before restarting; report Maven output to user |
| Frontend build fails | Fix TypeScript/Vite errors before restarting; report `npm run build` output |
| Port 5173 in use by other app | Read frontend terminal output for alternate Vite port |
| Frontend cannot reach API | Confirm backend health; CORS allows `http://localhost:5173` |
| `npm run dev` fails | Run `npm install` in `spendsense/frontend` |

## Out of scope

- Production deploy (Railway/Vercel) — see sprint Day 16
- Running tests — use the `run-tests` skill (`run tests`)
- Stopping servers — use the `stop-server` skill (`stop server`)

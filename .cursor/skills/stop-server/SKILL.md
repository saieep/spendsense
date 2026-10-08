---
name: stop-server
description: Stops SpendSense local dev servers — Spring Boot backend and Vite frontend together. Use when the user says "stop server", "stop servers", "shut down the app", "kill servers", or wants both backend and frontend stopped for spendsense development.
---

# Stop Server

Stops **both** SpendSense dev servers. Do not stop only one unless the user asks for backend or frontend alone.

**Plan reference:** [.cursor/plans/expense_tracker_mvp_d9134402.plan.md](../../plans/expense_tracker_mvp_d9134402.plan.md) — Local dev: Backend `http://localhost:8080`, Frontend `http://localhost:5173`

## Servers to stop

| Service | Port | Typical process |
|---------|------|-----------------|
| Backend | 8080 | Java / `spring-boot:run` |
| Frontend | 5173 | Node / Vite (`npm run dev`) |

## Workflow

1. **Check running servers** — list the terminals folder and check ports 8080 and 5173.
2. **Stop backend** — free port 8080.
3. **Stop frontend** — free port 5173.
4. **Confirm** — verify both ports are free (or report which are still in use).
5. **Tell the user** what was stopped and current port status.

If a server was not running, report that and continue stopping the other.

## Commands

### Windows (default for this workspace)

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
```

### macOS / Linux

```bash
lsof -ti:8080 | xargs -r kill -9
lsof -ti:5173 | xargs -r kill -9
```

## Verify ports are free

### Windows

```powershell
Get-NetTCPConnection -LocalPort 8080,5173 -ErrorAction SilentlyContinue
```

No output means both ports are free.

### macOS / Linux

```bash
lsof -i:8080 -i:5173
```

No output means both ports are free.

## Output template

```markdown
## Servers Stopped

| Service | Port | Status |
|---------|------|--------|
| Backend | 8080 | Stopped / Already stopped |
| Frontend | 5173 | Stopped / Already stopped |

Both dev servers are down.
```

## Troubleshooting

| Issue | Action |
|-------|--------|
| Port still in use after stop | Wait 2–3s and retry; use `netstat -ano \| findstr :8080` (Windows) to find PID |
| Vite on alternate port | Read frontend terminal output; stop that port too if user confirms |
| Cannot kill process | Report PID to user; may need elevated permissions |

## Related skills

- **Start servers:** `start-server` skill (`start server`)
- **Run tests:** `run-tests` skill (`run tests`)

## Out of scope

- Stopping production deploy (Railway/Vercel)
- Stopping test processes (`mvnw test`, `npm test`) — those exit on their own

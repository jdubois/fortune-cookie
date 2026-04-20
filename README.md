# 🔥 Devoxx Roaster

A tiny web app that **roasts** (nicely makes fun of) every talk from **Devoxx France 2026**.
Reload the page and get a fresh one-liner about a random session, with a link back to the
official talk page on the Devoxx companion.

Built for the conference, deployed on Azure, hosted entirely with pay-as-you-go
services that cost ~€0/month at this scale.

Live: <https://fortune-cookie-app.ambitiousbush-d26c8d80.francecentral.azurecontainerapps.io>

---

## Goal

- Have a bit of fun with the agenda of Devoxx France 2026.
- For each of the 257 talks, a bespoke **nerdy roast** is generated once and baked
  into a static JSON file. The roast only pokes at the topic or the tech — never at
  the speakers.
- The UI picks a random roast on each load, displays it in big, and shows the talk
  title as a reference with a link to the companion app.

## Tech stack

### Built with
- **[GitHub Copilot CLI](https://github.com/features/copilot)** — the entire codebase
  (backend, frontend, roasts, CI/CD) was written in pair with Copilot.
- **[Dr JSKill](https://github.com/jdubois/dr-jskill)** — a custom Copilot skill for
  scaffolding and evolving Java + Spring Boot projects.

### Backend
- **Java 25** (via the Maven wrapper)
- **Spring Boot 4** (Spring MVC, Actuator for health probes)
- **Jackson 3** (`tools.jackson.*`) to load `roasts.json` on startup
- **Maven** build (`./mvnw`)
- Two endpoints only:
  - `GET /api/roasts/random` → `{id, title, url, lang, roast}`
  - `GET /api/roasts/count` → `{total}`
- **GraalVM Native Image**: the production image is an AOT-compiled native binary.
  Runtime hints are registered for the JSON resource and record DTOs.

### Frontend
- **Vue 3.5** + **Vite 8**
- Vite builds directly into `src/main/resources/static/`, so the frontend is served
  by Spring Boot as plain static files — no separate CDN.
- **Bootstrap Icons** + a small custom CSS theme (Devoxx red `#e4002b` / orange
  `#ff6b1a`, dark gradient background, stylised "X" badge).

### Data
- `src/main/resources/roasts.json` — 257 entries generated from the public CFP API
  (`https://devoxxfr2026.cfp.dev/api/public/talks`).
- Each talk URL points at `https://m.devoxx.com/events/devoxxfr2026/talks/{id}/{slug}`,
  using the exact slugify rules the Devoxx companion app itself uses.

### CI / CD
- **GitHub Actions**
  - `.github/workflows/ci.yml` — runs unit tests on every push
  - `.github/workflows/azure-deploy.yml` — builds the native image, pushes it to
    **GitHub Container Registry** (`ghcr.io/jdubois/fortune-cookie:native`),
    then deploys it to Azure Container Apps

## Hosting

The app runs on **Azure Container Apps** (Consumption plan) in **France Central**:

| Setting | Value |
|---|---|
| Resource group | `fortune-cookie-rg` |
| Container App | `fortune-cookie-app` |
| Region | France Central |
| Replicas | min **0**, max **5** (scale-to-zero) |
| CPU / Memory | **0.25 vCPU / 0.5 GiB** |
| Ingress | Public HTTPS on port 8080 |
| Health probes | `/actuator/health/liveness` and `/actuator/health/readiness` |
| Auth to Azure | OIDC federated credential from GitHub Actions |
| Image pull | PAT on `ghcr.io` stored as `GHCR_PULL_PAT` |

Thanks to the GraalVM native image, cold start is **~100 ms** and steady-state
resident memory is **~80 MB**, so even worst-case traffic during the conference is
comfortably inside the Container Apps free tier.

## Running locally

```sh
# Backend + frontend in one go
cd frontend && npm install && npm run build && cd ..
./mvnw spring-boot:run
# → http://localhost:8080
```

Frontend dev mode with hot reload (proxies `/api` to Spring Boot on port 8080):

```sh
cd frontend
npm run dev
```

## Regenerating the roasts

The roasts are deliberately hand-crafted and committed as `roasts.json`.
To refresh the agenda (e.g. after the CFP updates):

1. Fetch the latest talks from `https://devoxxfr2026.cfp.dev/api/public/talks`.
2. Produce a new `roasts.json` — one entry per talk, with a fresh roast string —
   keeping the existing shape: `{id, title, url, lang, roast}`.
3. Rebuild and redeploy via a push to `main`.

## License

This project is for fun at Devoxx France 2026. All jokes are meant in good spirit
and target only the *topics and tech*, never the speakers. ❤️

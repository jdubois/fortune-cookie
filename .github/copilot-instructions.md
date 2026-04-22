# Copilot instructions — Devoxx Roaster (fortune-cookie)

Small full-stack app: a Spring Boot 4 / Java 25 backend serves a Vue 3 + Vite frontend plus two JSON endpoints backed by a static `roasts.json`. Deployed as a GraalVM native image to Azure Container Apps.

## Build, run, test

All commands run from the repo root unless noted.

- Backend build + tests: `./mvnw -B verify`
- Run backend only: `./mvnw spring-boot:run` (serves on `http://localhost:8080`)
- Run a single test: `./mvnw -Dtest=FortuneCookieApplicationTests test` (append `#methodName` for a single method)
- Frontend dev server (proxies `/api` to `:8080`): `cd frontend && npm run dev` (port 5173)
- Frontend production build: `cd frontend && npm install && npm run build` — outputs **directly into `src/main/resources/static/`** (see `frontend/vite.config.js`), so Spring Boot serves the built SPA; there is no separate frontend deploy.
- Full local run: build the frontend first, then `./mvnw spring-boot:run`.
- Native image (GraalVM 25+ required): `./mvnw native:compile -Pnative` → `target/fortune-cookie`. Tests in native mode: `./mvnw test -PnativeTest`.
- Container images: `Dockerfile` (JVM) and `Dockerfile-native` (AOT binary, this is what ships to prod via `ghcr.io/jdubois/fortune-cookie:native`).

Toolchain is pinned: Java 25 (Temurin), Node via `.nvmrc` / `.node-version` (Node 20.19+ or 22.12+). Use the Maven wrapper `./mvnw`, not a system Maven.

## Architecture

- **One controller, one data file.** `RoastController` (`src/main/java/com/example/fortunecookie/controller/`) loads `src/main/resources/roasts.json` once in `@PostConstruct` into an immutable `List<Roast>` and exposes:
  - `GET /api/roasts/random` → `Roast{id,title,url,lang,roast}`
  - `GET /api/roasts/count` → `Count{total}`
- **Jackson 3** is used directly (`tools.jackson.databind.json.JsonMapper`), *not* `com.fasterxml.jackson`. Spring Boot 4 ships Jackson 3 — don't add or import the old package.
- **GraalVM native compatibility is mandatory.** Any new class that is (de)serialized from JSON or loaded from the classpath at runtime must be registered via a `RuntimeHintsRegistrar` + `@ImportRuntimeHints`, like `RoastController.RoastHints` does for `Roast`, `Count`, and the `roasts.json` resource pattern. Missing hints break the native image silently in production but pass JVM tests.
- **Frontend is a single Vue SPA** (`frontend/src/App.vue` + `main.js`). It calls `/api/roasts/random` on load and links out to `https://m.devoxx.com/events/devoxxfr2026/talks/{id}/{slug}`. No router, no state store.
- **Actuator health probes** (`/actuator/health/liveness`, `/actuator/health/readiness`) are the contract with Azure Container Apps — keep them exposed in `application.properties`.

## Key conventions

- `roasts.json` is the product. It's hand-curated and committed; don't regenerate it automatically in build steps. Preserve the shape `{id, title, url, lang, roast}` — both the backend record and the frontend read these exact fields.
- Roasts target **topics/tech only, never speakers**. Honor this if asked to add or edit entries.
- Keep the backend tiny: no database, no extra starters, no new endpoints unless asked. The `spring-boot-starter-*-test` dependencies in `pom.xml` are the Spring Boot 4 test slices (`actuator-test`, `validation-test`, `webmvc-test`) — use those instead of pulling in `spring-boot-starter-test`.
- Frontend assets: Vite writes to `src/main/resources/static/` with `emptyOutDir: true`. Don't hand-edit files under `src/main/resources/static/` — they are build output and will be clobbered.
- CI (`.github/workflows/ci.yml`) runs `./mvnw -B verify` then a frontend `npm ci && npm run build`. The `azure-deploy.yml` workflow builds the **native** image from `Dockerfile-native` and pushes to GHCR before deploying to Azure Container Apps. If you change the Dockerfiles, verify both still build.
- This codebase was bootstrapped with the **Dr JSKill** Copilot skill for Spring Boot scaffolding; prefer the existing structure (`com.example.fortunecookie.{config,controller}`) over reorganizing.
